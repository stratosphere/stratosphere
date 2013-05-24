package eu.stratosphere.pact.runtime.hash;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.stratosphere.nephele.services.iomanager.IOManager;
import eu.stratosphere.nephele.services.memorymanager.MemoryAllocationException;
import eu.stratosphere.nephele.services.memorymanager.MemoryManager;
import eu.stratosphere.nephele.services.memorymanager.MemorySegment;
import eu.stratosphere.nephele.template.AbstractInvokable;
import eu.stratosphere.pact.common.util.MutableObjectIterator;
import eu.stratosphere.pact.generic.types.TypeComparator;
import eu.stratosphere.pact.generic.types.TypePairComparator;
import eu.stratosphere.pact.generic.types.TypeSerializer;

public class BuildFirstHashMultiMatchIterator<V1, V2, O> extends BuildFirstHashMatchIterator<V1, V2, O> {

	
	final private MultiMatchMutableHashTable<V1, V2> multiHashTable;
	
	public BuildFirstHashMultiMatchIterator(
			MutableObjectIterator<V1> firstInput,
			MutableObjectIterator<V2> secondInput,
			TypeSerializer<V1> serializer1, TypeComparator<V1> comparator1,
			TypeSerializer<V2> serializer2, TypeComparator<V2> comparator2,
			TypePairComparator<V2, V1> pairComparator,
			MemoryManager memManager, IOManager ioManager,
			AbstractInvokable ownerTask, long totalMemory)
			throws MemoryAllocationException {
		super(firstInput, secondInput, serializer1, comparator1, serializer2,
				comparator2, pairComparator, memManager, ioManager, ownerTask,
				totalMemory);
		multiHashTable = (MultiMatchMutableHashTable<V1, V2>) hashJoin;
	}

	public <BT, PT> MutableHashTable<BT, PT> getHashJoin(TypeSerializer<BT> buildSideSerializer, TypeComparator<BT> buildSideComparator,
			TypeSerializer<PT> probeSideSerializer, TypeComparator<PT> probeSideComparator,
			TypePairComparator<PT, BT> pairComparator,
			MemoryManager memManager, IOManager ioManager, AbstractInvokable ownerTask, long totalMemory)
	throws MemoryAllocationException
	{
		totalMemory = memManager.roundDownToPageSizeMultiple(totalMemory);
		final int numPages = (int) (totalMemory / memManager.getPageSize());
		final List<MemorySegment> memorySegments = memManager.allocatePages(ownerTask, numPages);
		return new MultiMatchMutableHashTable<BT, PT>(buildSideSerializer, probeSideSerializer, buildSideComparator, probeSideComparator, pairComparator, memorySegments, ioManager);
	}
	
	/**
	 * Set new input for probe side
	 */
	public void reopenProbe(MutableObjectIterator<V2> probeInput) {
		try {
			// FIXME: Not sure if this is the right place to catch the exception.
			multiHashTable.reopenProbe(probeInput);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
