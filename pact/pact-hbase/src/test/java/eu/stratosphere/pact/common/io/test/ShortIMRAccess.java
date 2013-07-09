/**
 * 
 */
package eu.stratosphere.pact.common.io.test;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;

import eu.stratosphere.pact.common.io.HBaseKey;
import eu.stratosphere.pact.common.io.HBaseResult;
import eu.stratosphere.pact.common.io.TableInputFormat;
import eu.stratosphere.pact.common.io.TableInputSplit;
import eu.stratosphere.pact.common.type.PactRecord;

/**
 * @author Mathias Peters <mathias.peters@informatik.hu-berlin.de>
 *
 */
public class ShortIMRAccess {
	
	private static Log LOG = LogFactory.getLog("ShortIMRAccess");
	
	/**
	 * Quick and dirty tutorial to connect to IMRs HBase servers and read 1k 
	 * records from the collection_schmema table.
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		eu.stratosphere.nephele.configuration.Configuration parameters = new eu.stratosphere.nephele.configuration.Configuration();
		parameters.setString(TableInputFormat.INPUT_TABLE, "test");
		parameters.setString(TableInputFormat.CONFIG_LOCATION, 
				"file:///etc/hbase/conf/hbase-site.xml");

		TableInputFormat tif = new TableInputFormat();
		tif.configure(parameters);
		
		TableInputSplit[] createInputSplits = tif.createInputSplits(0);
		TableInputSplit inputSplit = createInputSplits[0];
		tif.open(inputSplit);
		
		PactRecord record = new PactRecord();
		int count = 0;
		while(tif.nextRecord(record) )
		{
				count++;
				HBaseKey currentKey = record.getField(0, HBaseKey.class);
				HBaseResult result = record.getField(1, HBaseResult.class);
				System.out.println("count: "+count+" end = " + tif.reachedEnd() +"key:\t" + currentKey.getWritable() + "; value:\t" + result.getResult().toString());

		}
		System.out.println("end of test");
	}

}
