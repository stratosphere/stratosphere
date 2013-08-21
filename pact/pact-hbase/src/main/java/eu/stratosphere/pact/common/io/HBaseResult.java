package eu.stratosphere.pact.common.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.hbase.client.Result;

import eu.stratosphere.pact.common.type.Value;

public class HBaseResult implements Value
{
	private Result result;
	
	
	public HBaseResult() {
		this.result = new Result();
	}
	
	public HBaseResult(Result result) {
		this.result = result;
	}
	
	
	public Result getResult() {
		return this.result;
	}
	
	public void setResult(Result result)
	{
		this.result = result;
	}
	
	public String getStringdata()
	{
		if(this.result != null)
		{
			return this.result.toString();
		}
		return null;
	}
	
	@Override
	public void read(DataInput in) throws IOException {
		this.result.readFields(in);
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		this.result.write(out);	
	}
}
