package eu.stratosphere.nephele.services.accumulators;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import eu.stratosphere.nephele.execution.librarycache.LibraryCacheManager;
import eu.stratosphere.nephele.io.IOReadableWritable;
import eu.stratosphere.nephele.jobgraph.JobID;
import eu.stratosphere.nephele.util.SerializableHashMap;
import eu.stratosphere.nephele.util.StringUtils;

/**
 * 
 * 
 * This class doesn't use the existing {@link SerializableHashMap} since this
 * requires to transform the string to a StringRecord. It also sends the class
 * of the key, which is constant in our case.
 */
public class AccumulatorEvent implements IOReadableWritable {

	private JobID jobID;

	private Map<String, Accumulator<?, ?>> accumulators = new HashMap<String, Accumulator<?, ?>>();

	public AccumulatorEvent() {

	}

	public AccumulatorEvent(JobID jobID,
			Map<String, Accumulator<?, ?>> accumulators) {
		this.accumulators = accumulators;
		this.jobID = jobID;
	}

	public JobID getJobID() {
		return this.jobID;
	}

	public Map<String, Accumulator<?, ?>> getAccumulators() {
		return this.accumulators;
	}

	@Override
	public void write(DataOutput out) throws IOException {
		jobID.write(out);
		out.writeInt(accumulators.size());
		for (Map.Entry<String, Accumulator<?, ?>> entry : this.accumulators
				.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeUTF(entry.getValue().getClass().getName());
			entry.getValue().write(out);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void read(DataInput in) throws IOException {
		jobID = new JobID();
		jobID.read(in);
		int numberOfMapEntries = in.readInt();
		this.accumulators = new HashMap<String, Accumulator<?, ?>>(
				numberOfMapEntries);
		
		// Get user class loader. Required to support custom accumulators.
		ClassLoader clazzLoader = LibraryCacheManager.getClassLoader(jobID);

		for (int i = 0; i < numberOfMapEntries; i++) {
			System.out.println("- read entry " + i);
			String key = in.readUTF();

			final String valueType = in.readUTF();
			Class<Accumulator<?, ?>> valueClass = null;
			try {
				valueClass = (Class<Accumulator<?, ?>>) Class.forName(valueType, true, clazzLoader);
			} catch (ClassNotFoundException e) {
				throw new IOException(StringUtils.stringifyException(e));
			}

			Accumulator<?, ?> value = null;
			try {
				value = valueClass.newInstance();
			} catch (Exception e) {
				throw new IOException(StringUtils.stringifyException(e));
			}
			value.read(in);

			this.accumulators.put(key, value);
		}
	}

}
