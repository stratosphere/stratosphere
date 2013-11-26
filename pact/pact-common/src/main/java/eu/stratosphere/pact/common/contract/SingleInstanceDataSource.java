package eu.stratosphere.pact.common.contract;


import eu.stratosphere.pact.generic.io.InputFormat;

/**
 * This DataSource enforces a degree of parallelism of one.
 * 
 * It can be used for InputFormats that read from sources that are not splitable (such as databases or network resources).
 * 
 */
public class SingleInstanceDataSource<T extends InputFormat<?, ?>> extends GenericDataSource<T> {

	public SingleInstanceDataSource(Class<? extends T> format, String name) {
		super(format, name);
		super.setDegreeOfParallelism(1);
	}

	public SingleInstanceDataSource(T format) {
		super(format);
		super.setDegreeOfParallelism(1);
	}

	public SingleInstanceDataSource(T format, String name) {
		super(format, name);
		super.setDegreeOfParallelism(1);
	}

	public SingleInstanceDataSource(Class<? extends T> format) {
		super(format);
		super.setDegreeOfParallelism(1);
	}
	
	@Override
	public void setDegreeOfParallelism(int degree) {
		if(degree != 1) {
			throw new RuntimeException("The SingleInstanceDataSource allows only a DOP of 1");
		}
		super.setDegreeOfParallelism(1);
	}
}
