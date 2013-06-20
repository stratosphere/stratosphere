package eu.stratosphere.pact.test.debugging;

import java.io.IOException;

import eu.stratosphere.nephele.client.JobClient;
import eu.stratosphere.nephele.client.JobExecutionException;
import eu.stratosphere.nephele.jobgraph.JobGraph;
import eu.stratosphere.pact.client.LocalExecutor;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.compiler.DataStatistics;
import eu.stratosphere.pact.compiler.PactCompiler;
import eu.stratosphere.pact.compiler.plan.candidate.OptimizedPlan;
import eu.stratosphere.pact.compiler.plantranslate.NepheleJobGraphGenerator;
import eu.stratosphere.pact.example.kmeans.KMeansIterative;



public class KMeansRun {
    public static void main(String[] args) throws Exception{
        
        
        KMeansIterative kmi = new KMeansIterative();
        Plan plan = kmi.getPlan(args);
        LocalExecutor.execute(plan);
    }

}
