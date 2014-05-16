package eu.stratosphere.example.java.lambdas;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.tuple.Tuple1;

public class SimpleLambdaExample {
	
	public static void main(String[] args) throws Exception {
		ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
		
		DataSet<String> strings = env.fromElements("A", "B", "C", "D");
		
		DataSet<Tuple1<Boolean>> result = strings.map((letter) -> new Tuple1<Boolean>(letter.equals("A")));
		
		result.print();
		
		env.execute();
	}
	
}
