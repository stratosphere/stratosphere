package eu.stratosphere.pact.runtime.hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.SysexMessage;


import eu.stratosphere.nephele.services.iomanager.BlockChannelReader;
import eu.stratosphere.nephele.services.iomanager.BlockChannelWriter;
import eu.stratosphere.nephele.services.iomanager.BulkBlockChannelReader;
import eu.stratosphere.nephele.services.iomanager.Channel;
import eu.stratosphere.nephele.services.iomanager.ChannelReaderInputView;
import eu.stratosphere.nephele.services.iomanager.ChannelWriterOutputView;
import eu.stratosphere.nephele.services.iomanager.IOManager;
import eu.stratosphere.nephele.services.memorymanager.MemorySegment;
import eu.stratosphere.pact.common.util.MutableObjectIterator;
import eu.stratosphere.pact.generic.types.TypeComparator;
import eu.stratosphere.pact.generic.types.TypePairComparator;
import eu.stratosphere.pact.generic.types.TypeSerializer;
import eu.stratosphere.pact.runtime.io.ChannelReaderInputViewIterator;

public class MultiMatchMutableHashTable<BT, PT> extends MutableHashTable<BT, PT> {

	/**
	 * Channel for the spilled partitions
	 */
	private final Channel.Enumerator spilledInMemoryPartitions;
	
	/**
	 * Stores the initial partitions and a list of the files that contain the spilled contents
	 */
	private List<HashPartition<BT, PT>> initialPartitions;
	

	/**
	 * The values of these variables are stored here after the initial open()
	 * Required to restore the initial state before each additional probe phase.
	 */
	private int initialBucketCount;
	private byte initialPartitionFanOut;

	
	private boolean spilled = false;

	
	public MultiMatchMutableHashTable(TypeSerializer<BT> buildSideSerializer,
			TypeSerializer<PT> probeSideSerializer,
			TypeComparator<BT> buildSideComparator,
			TypeComparator<PT> probeSideComparator,
			TypePairComparator<PT, BT> comparator,
			List<MemorySegment> memorySegments, IOManager ioManager) {
		super(buildSideSerializer, probeSideSerializer, buildSideComparator,
				probeSideComparator, comparator, memorySegments, ioManager);
		isMultiHashTable = true;
		spilledInMemoryPartitions = ioManager.createChannelEnumerator();
	}
	
	@Override
	public void open(MutableObjectIterator<BT> buildSide,
			MutableObjectIterator<PT> probeSide) throws IOException {
		memstats("Before open call");
		super.open(buildSide, probeSide);
		initialPartitions = new ArrayList<HashPartition<BT, PT>>( partitionsBeingBuilt );
		initialPartitionFanOut = (byte) partitionsBeingBuilt.size();
		initialBucketCount = this.numBuckets;
		memstats("after open call");
	}

	public void reopenProbe(MutableObjectIterator<PT> probeInput) throws IOException {
		System.err.println("Opening new probe Input");
		memstats("new input");
		if(closed) {
			throw new IllegalStateException("Cannot open probe input because hash join has already been closed");
		}
		partitionsBeingBuilt.clear();
		probeIterator = new ProbeIterator<PT>(probeInput, probeSideSerializer.createInstance());
		// We restore the same "partitionsBeingBuild" state as after the initial open call.
		partitionsBeingBuilt.addAll(initialPartitions);
		
		if(spilled) {
			System.err.println("build side is spilled -> init and restore table");
			this.currentRecursionDepth = 0;
			initTable(initialBucketCount, initialPartitionFanOut);
			
			//setup partitions for insertion:
			int cnt = 0;
			for (int i = 0; i < this.partitionsBeingBuilt.size(); i++) {
				HashPartition<BT, PT> part = this.partitionsBeingBuilt.get(i);
				if(part.isInMemory()) {
					ensureNumBuffersReturned(part.initialPartitionBuffersCount);
					part.restorePartitionBuffers(ioManager, availableMemory);
					// CODE FROM buildTableFromSpilledPartition()
					// now, index the partition through a hash table
					final HashPartition<BT, PT>.PartitionIterator pIter = part.getPartitionIterator(this.buildSideComparator);
					final BT record = this.buildSideSerializer.createInstance();
					
					while (pIter.next(record)) {
						final int hashCode = hash(pIter.getCurrentHashCode(), 0);
						final int posHashCode = hashCode % initialBucketCount;
						final long pointer = pIter.getPointer();
						
						// get the bucket for the given hash code
						final int bucketArrayPos = posHashCode >> this.bucketsPerSegmentBits;
						final int bucketInSegmentPos = (posHashCode & this.bucketsPerSegmentMask) << NUM_INTRA_BUCKET_BITS;
						final MemorySegment bucket = this.buckets[bucketArrayPos];
						cnt++;
						insertBucketEntry(part, bucket, bucketInSegmentPos, hashCode, pointer);
					}
				} else {
					this.writeBehindBuffersAvailable--; // we are not in-memory, thus the probe side buffer will grab one wbb.
					if(this.writeBehindBuffers.size() == 0) { // prepareProbePhase always requires one buffer in the writeBehindBuffers-Queue.
						this.writeBehindBuffers.add(getNextBuffer());
						this.writeBehindBuffersAvailable++;
					}
					part.prepareProbePhase(ioManager,currentEnumerator,writeBehindBuffers, null);
				}
			}
			System.err.println("Restored "+cnt+" in memory tuples");
			// spilled partitions are automatically added as pending partitions after in-memory has been handled
		} else {
			// the build input completely fits into memory, hence everything is still in memory.
			for (int partIdx = 0; partIdx < partitionsBeingBuilt.size(); partIdx++) {
				final HashPartition<BT, PT> p = partitionsBeingBuilt.get(partIdx);
				try {
					p.prepareProbePhase(ioManager,currentEnumerator,writeBehindBuffers, null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		memstats("After openSecondInput");
	}
	

	/**
	 * This method stores the initial hash table's contents on disk if hash join needs the memory
	 * for further partition processing.
	 * The initial hash table is rebuild before a new secondary input is opened.
	 * 
	 * For the sake of simplicity we iterate over all in-memory elements and store them in one file.
	 * The file is hashed into memory upon opening a new probe input.
	 * @throws IOException 
	 */
	void storeInitialHashTable() throws IOException {
		if(spilled) {
			System.err.println("The initial table has already been spilled!");
			return; // we create the initialHashTable only once. Later calls are caused by deeper recursion lvls
		}
		spilled = true;
		// Stephan suggested to use spillPartition here.
		// - downside: it will force all partitions on disk
		System.err.println("Spilling in-memory partitions to disk");
		
		for (int partIdx = 0; partIdx < initialPartitions.size(); partIdx++) {
			final HashPartition<BT, PT> p = initialPartitions.get(partIdx);
			if( p.isInMemory()) { // write memory resident partitions to disk
				this.writeBehindBuffersAvailable += p.spillInMemoryPartition(spilledInMemoryPartitions.next(), ioManager, writeBehindBuffers);
			}
		}
		memstats("Return memory from write behind (spilling done)");
	}
	
	@Override
	public void close() {
		if(partitionsBeingBuilt.size() == 0) { // partitions are cleared after the build phase. But we need to drop
			// memory with them.
			this.partitionsBeingBuilt.addAll(initialPartitions);
		}
		super.close();
		memstats("after close");
	}
}
