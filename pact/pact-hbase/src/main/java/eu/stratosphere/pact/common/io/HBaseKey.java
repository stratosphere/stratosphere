package eu.stratosphere.pact.common.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.io.ImmutableBytesWritable;

import eu.stratosphere.pact.common.type.Value;

/**
 * Simple wrapper to encapsulate an {@link ImmutableBytesWritable}. 
 * @author Mathias Peters <mathias.peters@informatik.hu-berlin.de>
 *
 */
public class HBaseKey implements Value{

	private ImmutableBytesWritable writable;
	
	/**
	 * Default Ctor.
	 */
	public HBaseKey()
	{
	}
	
	public HBaseKey(ImmutableBytesWritable writable)
	{
		this.writable = writable;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.writable.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException {
		this.writable.readFields(in);
	}

	public ImmutableBytesWritable getWritable() {
		return writable;
	}

	public void setWritable(ImmutableBytesWritable writable) {
		this.writable = writable;
	}

}
