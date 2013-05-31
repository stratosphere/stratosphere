package eu.stratosphere.nephele.io.channels;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.stratosphere.nephele.services.memorymanager.MemorySegment;
import eu.stratosphere.nephele.util.BufferPoolConnector;


public class TestMemoryBuffer {

	private MemoryBufferPoolConnector bufferPoolConnector;
	private Queue<MemorySegment> bufferPool;
	
	private final static int INT_COUNT = 512;
	private final static int INT_SIZE = Integer.SIZE / Byte.SIZE;

	@Before
	public void setUp() throws Exception {
		bufferPool = new LinkedBlockingQueue<MemorySegment>();
		bufferPoolConnector = new BufferPoolConnector(bufferPool);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void readToSmallByteBuffer() throws IOException {
		ReferenceMemoryBuffer ref = new ReferenceMemoryBuffer(INT_COUNT*INT_SIZE, ByteBuffer.allocate(INT_COUNT*INT_SIZE), bufferPoolConnector);
		fillBuffer(ref);
		
		MemoryBuffer buf = new MemoryBuffer(INT_COUNT*INT_SIZE, new MemorySegment(new byte[INT_COUNT*INT_SIZE], 0, INT_COUNT*INT_SIZE), bufferPoolConnector);
		fillBuffer(buf);
		
		ByteBuffer target = ByteBuffer.allocate(INT_SIZE);
		ByteBuffer largeTarget = ByteBuffer.allocate(INT_COUNT*INT_SIZE);
		int i = 0;
		System.err.println("Starting to read");
		while(buf.hasRemaining()) {
			buf.read(target);
			target.rewind();
			largeTarget.put(target);
			target.rewind();
			if( i++ >= INT_COUNT) {
				fail("There were too many elements in the buffer");
			}
		}
		assertEquals(-1, buf.read(target));
		
		target.rewind();
		validateByteBuffer(largeTarget);
	}
		
	@Test
	public void readToByteBuffer() throws IOException {
		
		ReferenceMemoryBuffer ref = new ReferenceMemoryBuffer(INT_COUNT*INT_SIZE, ByteBuffer.allocate(INT_COUNT*INT_SIZE), bufferPoolConnector);
		fillBuffer(ref);
		
		MemoryBuffer buf = new MemoryBuffer(INT_COUNT*INT_SIZE, new MemorySegment(new byte[INT_COUNT*INT_SIZE], 0, INT_COUNT*INT_SIZE), bufferPoolConnector);
		fillBuffer(buf);
		
		ByteBuffer target = ByteBuffer.allocate(INT_COUNT*INT_SIZE);
		
		// test for similar starting position
		assertEquals(ref.getByteBuffer().limit(), buf.limit());
		assertEquals(ref.getByteBuffer().position(), buf.position());
		
		// call to be tested!
		buf.read(target);
		final int actLim = buf.limit();
		final int actPos = buf.position();
		
		validateByteBuffer(target);
		final int actLim1 = buf.limit();
		final int actPos1 = buf.position();
		
		target.clear();
		
		ref.read(target);
		assertEquals(ref.getByteBuffer().limit(), actLim);
		assertEquals(ref.getByteBuffer().position(), actPos);
		
		validateByteBuffer(target);
		assertEquals(ref.getByteBuffer().limit(), actLim1);
		assertEquals(ref.getByteBuffer().position(), actPos1);
		
		buf.close(); // make eclipse happy
	}
	
	/**
	 * CopyToBuffer uses system.arraycopy()
	 * 
	 * @throws IOException
	 */
	@Test
	public void copyToBufferTest() throws IOException {

		MemoryBuffer buf = new MemoryBuffer(INT_COUNT*INT_SIZE, new MemorySegment(new byte[INT_COUNT*INT_SIZE], 0, INT_COUNT*INT_SIZE), bufferPoolConnector);
		fillBuffer(buf);
		
		
		// the target buffer is larger to check if the limit is set appropriately
		MemoryBuffer destination = new MemoryBuffer(INT_COUNT*INT_SIZE*2, 
					new MemorySegment(new byte[INT_COUNT*INT_SIZE*2],0,INT_COUNT*INT_SIZE*2), 
					bufferPoolConnector);
		assertEquals(INT_COUNT*INT_SIZE*2, destination.limit());
		// copy buf contents to double sized MemBuffer
		buf.copyToBuffer(destination);
		assertEquals(INT_COUNT*INT_SIZE, destination.limit());
		
		// copy contents of destination to byteBuffer
		ByteBuffer test = ByteBuffer.allocate(INT_COUNT*INT_SIZE);
		int written = destination.read(test);
		assertEquals(INT_COUNT*INT_SIZE, written);
		// validate byteBuffer contents
		validateByteBuffer(test);
		
		destination.position(written);
		destination.limit(destination.getTotalSize());
		// allocate another byte buffer to write the rest of destination into a byteBuffer
		ByteBuffer testRemiander = ByteBuffer.allocate(INT_COUNT*INT_SIZE);
		written = destination.read(testRemiander);
		assertEquals(INT_COUNT*INT_SIZE, written);
		expectAllNullByteBuffer(testRemiander);
		
		buf.close(); // make eclipse happy
	}

	private void fillBuffer(Buffer buf) throws IOException {
		ByteBuffer src = ByteBuffer.allocate(INT_SIZE);
		// write some data into buf:
		for(int i = 0; i < INT_COUNT; ++i) {
			src.putInt(0,i);
			src.rewind();
			int written = buf.write(src);
		//	System.err.println("Put int i="+i+" Written "+written);
		}
		buf.finishWritePhase();
	}
	
	
	/**
	 * Validates if the ByteBuffer contains the what fillMemoryBuffer has written!
	 * 
	 * @param target
	 */
	private void validateByteBuffer(ByteBuffer target) {
		ByteBuffer ref = ByteBuffer.allocate(INT_SIZE);
		
		for(int i = 0; i < INT_SIZE*INT_COUNT; ++i) {
			ref.putInt(0,i / INT_SIZE);
			assertEquals("Byte at position "+i+" is different", ref.get(i%INT_SIZE), target.get(i));
		}
	}
	
	private void expectAllNullByteBuffer(ByteBuffer target) {
		ByteBuffer ref = ByteBuffer.allocate(INT_SIZE);
		ref.putInt(0,0);
		for(int i = 0; i < INT_COUNT; ++i) {
			assertEquals("Byte at position "+i+" is different", ref.getInt(0), target.getInt(i));
		}
	}
}
