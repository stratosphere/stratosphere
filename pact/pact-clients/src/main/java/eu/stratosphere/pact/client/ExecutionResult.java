package eu.stratosphere.pact.client;

import java.util.Map;

public class ExecutionResult {
	
	private long netRuntime;
	private Map<String, Long> longCounters;
	
	public ExecutionResult(long netRuntime, Map<String, Long> longCounters) {
		this.netRuntime = netRuntime;
		this.longCounters = longCounters;
	}
	
	public long getNetRuntime() {
		return netRuntime;
	}
	
	public Map<String, Long> getLongCounters() {
		return longCounters;
	}

}
