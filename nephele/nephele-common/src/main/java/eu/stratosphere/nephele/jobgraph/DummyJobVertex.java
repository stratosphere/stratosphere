package eu.stratosphere.nephele.jobgraph;

public class DummyJobVertex extends AbstractJobVertex {

	private static DummyJobVertex dummyVertex = null;
	
	private DummyJobVertex(String name, JobVertexID id, JobGraph jobGraph) {
		super(name, id, jobGraph);
	}
	
	public static DummyJobVertex getInstance(JobGraph jobGraph) {
		if (dummyVertex == null) {
			dummyVertex = new DummyJobVertex("dummy", new JobVertexID(), jobGraph);
		}
		return dummyVertex;
	}
	

}
