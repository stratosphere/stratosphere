package eu.stratosphere.pact.example.localdemo;

import eu.stratosphere.nephele.client.JobClient;
import eu.stratosphere.nephele.client.JobExecutionException;
import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.nephele.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.jobgraph.JobGraph;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.compiler.DataStatistics;
import eu.stratosphere.pact.compiler.PactCompiler;
import eu.stratosphere.pact.compiler.costs.DefaultCostEstimator;
import eu.stratosphere.pact.compiler.plan.candidate.OptimizedPlan;
import eu.stratosphere.pact.compiler.plantranslate.NepheleJobGraphGenerator;
import eu.stratosphere.pact.example.pagerank.DanglingPageRank;

import java.io.IOException;
import java.net.InetSocketAddress;

public class RunPageRank {

  public static void main(String[] args) throws IOException, JobExecutionException {

    DanglingPageRank pageRank = new DanglingPageRank();

    Plan plan = pageRank.getPlan(
        "4",
        "file:///home/ssc/Entwicklung/datasets/slashdotzoo/demo/pagerank/vertices/",
        "file:///home/ssc/Entwicklung/datasets/slashdotzoo/demo/pagerank/adjacency/",
        "file:///tmp/pr/out.txt",
        String.valueOf(50),
        String.valueOf(82140),
        String.valueOf(38096)
    );

    PactCompiler pc = new PactCompiler(new DataStatistics(), new DefaultCostEstimator(),
        new InetSocketAddress("localhost", 6123));
    OptimizedPlan op = pc.compile(plan);

    NepheleJobGraphGenerator jgg = new NepheleJobGraphGenerator();
    JobGraph jobGraph = jgg.compileJobGraph(op);

    GlobalConfiguration.loadConfiguration("/home/ssc/Desktop/stratosphere/local-conf/");
    Configuration conf = GlobalConfiguration.getConfiguration();
    JobClient client = new JobClient(jobGraph, conf);
    client.submitJobAndWait();

  }
}
