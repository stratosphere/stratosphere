package eu.stratosphere.pact.example.jdbcinput;

import eu.stratosphere.nephele.configuration.Configuration;
import eu.stratosphere.pact.client.LocalExecutor;
import eu.stratosphere.pact.common.contract.FileDataSink;
import eu.stratosphere.pact.common.contract.GenericDataSource;
import eu.stratosphere.pact.common.io.JDBCInputFormat;
import eu.stratosphere.pact.common.io.RecordOutputFormat;
import eu.stratosphere.pact.common.plan.Plan;
import eu.stratosphere.pact.common.plan.PlanAssembler;
import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
import eu.stratosphere.pact.common.type.base.PactFloat;
import eu.stratosphere.pact.common.type.base.PactInteger;
import eu.stratosphere.pact.common.type.base.PactString;

/**
 * DB Schema
 *
 * ID  | title   | author  | price | qty 
 * int | varchar | varchar | float | int
 */
public class JDBCInputExample implements PlanAssembler, PlanAssemblerDescription {

        public static void execute(Plan toExecute) throws Exception {
                LocalExecutor executor = new LocalExecutor();
                executor.start();
                long runtime = executor.executePlan(toExecute);
                System.out.println("runtime:  " + runtime);
                executor.stop();
        }

        @Override
        public Plan getPlan(String[] args) {
                //String query = args[0];
                String query = "select * from books;";
                //String output = args[1];
                String output = "file://c:/TEST/output.txt";

                GenericDataSource source = new GenericDataSource(new JDBCInputFormat(query), "Data Source");
                source.setParameter("type", "mysql");
                source.setParameter("host", "127.0.0.1");
                source.setParameter("port", 3306);
                source.setParameter("name", "ebookshop");
                source.setParameter("username", "root");
                source.setParameter("password", "1111");

                FileDataSink sink = new FileDataSink(new RecordOutputFormat(), output, "Data Output");
                RecordOutputFormat.configureRecordFormat(sink)
                        .recordDelimiter('\n')
                        .fieldDelimiter(' ')
                        .field(PactInteger.class, 0)
                        .field(PactString.class, 1)
                        .field(PactString.class, 2)
                        .field(PactFloat.class, 3)
                        .field(PactInteger.class, 4);

                sink.addInput(source);
                return new Plan(sink, "JDBC Input Example Job");
        }

        @Override
        public String getDescription() {
                return "Parameter: [Query] [Output File]"; // TODO
        }

        // You can run this using:
        // mvn exec:exec -Dexec.executable="java" -Dexec.args="-cp %classpath eu.stratosphere.quickstart.RunJob <args>"
        public static void main(String[] args) throws Exception {
                JDBCInputExample tut = new JDBCInputExample();
                Plan toExecute = tut.getPlan(args);
                execute(toExecute);
        }
}
