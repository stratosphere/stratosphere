package eu.stratosphere.pact.example.jdbcinput;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.client.LocalExecutor;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.GenericDataSink;
import eu.stratosphere.pact.common.contract.GenericDataSource;
import eu.stratosphere.pact.common.io.DelimitedOutputFormat;
import eu.stratosphere.pact.common.io.JDBCInputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;

/**
 * This is a outline for a Stratosphere job.
 *
 * See the comments in getPlan() below on how to start with your job!
 *
 * You can run it out of your IDE using the main() method. This will use the
 * LocalExecutor to start a little Stratosphere instance out of your IDE.
 *
 * You can also generate a .jar file that you can submit on your Stratosphere
 * cluster. Just type mvn clean package in the projects root directory. You will
 * find the jar in target/stratosphere-quickstart-0.1-SNAPSHOT-Sample.jar
 *
 */
public class JDBCInputExample implements PlanAssembler, PlanAssemblerDescription {

        public static void execute(Plan toExecute) throws Exception {
                LocalExecutor executor = new LocalExecutor();
                executor.start();
                long runtime = executor.executePlan(toExecute);
                System.out.println("runtime:  " + runtime);
                executor.stop();
        }

        public Plan getPlan() {
                String output = "output.txt";
                Configuration config = new Configuration();
                config.setString("type", "mysql");
                config.setString("host", "127.0.0.1");
                config.setInteger("port", 3306);
                config.setString("name", "ebookshop");
                config.setString("username", "root");
                config.setString("password", "1111");
                GenericDataSource source = new GenericDataSource(new JDBCInputFormat(config, "select * from books;"), "Data Source");

                GenericDataSink sink = new FileDataSink(DelimitedOutputFormat.class, output);

                sink.addInput(source);
                return new Plan(sink, "JDBC Input Example Job");
        }

        public String getDescription() {
                return "Usage: ... "; // TODO
        }

        // You can run this using:
        // mvn exec:exec -Dexec.executable="java" -Dexec.args="-cp %classpath eu.stratosphere.quickstart.RunJob <args>"
        public static void main(String[] args) throws Exception {
                JDBCInputExample tut = new JDBCInputExample();
                Plan toExecute = tut.getPlan();
                execute(toExecute);
        }

        @Override
        public Plan getPlan(String... args) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
}
