/***********************************************************************************************************************
 * Copyright (C) 2010-2014 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/
package eu.stratosphere.test.localDistributed;

import eu.stratosphere.client.localDistributed.LocalDistributedExecutor;
import eu.stratosphere.example.java.record.connectedcomponents.WorksetConnectedComponents;
import eu.stratosphere.example.java.record.wordcount.WordCount;
import eu.stratosphere.test.iterative.nephele.ConnectedComponentsNepheleITCase;
import eu.stratosphere.test.testdata.ConnectedComponentsData;
import eu.stratosphere.test.testdata.WordCountData;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;

import static java.io.File.createTempFile;

/**
 * Tests run with the LocalDistributedExecutor execute in CLUSTER mode with multiple TaskManagers.
 * <p>
 * The data transfer happens over the TCP/IP stack of the runtime. Other tests, e.g. those based on the
 * {@link eu.stratosphere.test.util.TestBase2}), only operate in-memory and copy buffers from input to output channels
 * without going through TCP/IP.
 */
public class LocalDistributedExecutorTest {

	@Test
	public void testLocalDistributedExecutorWithWordCount() {

		LocalDistributedExecutor lde = new LocalDistributedExecutor();

		try {
			// set up the files
			File inFile = createTempFile("wctext", ".in");
			File outFile = createTempFile("wctext", ".out");
			inFile.deleteOnExit();
			outFile.deleteOnExit();
			
			FileWriter fw = new FileWriter(inFile);
			fw.write(WordCountData.TEXT);
			fw.close();
			
			// run WordCount
			WordCount wc = new WordCount();

			lde.start(2);
			lde.run(wc.getPlan("4", "file://" + inFile.getAbsolutePath(), "file://" + outFile.getAbsolutePath()));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} finally {
			try {
				lde.stop();
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
		}
	}

	@Test
	public void testLocalDistributedExecutorWithConnectedComponents() {
		LocalDistributedExecutor lde = new LocalDistributedExecutor();

		final long SEED = 0xBADC0FFEEBEEFL;

		final int NUM_VERTICES = 1000;

		final int NUM_EDGES = 10000;

		try {
			// set up the files
			File verticesFile = createTempFile("vertices", ".txt");
			File edgesFile = createTempFile("edges", ".txt");
			File resultFile = createTempFile("results", ".txt");

			verticesFile.deleteOnExit();
			edgesFile.deleteOnExit();
			resultFile.deleteOnExit();

			FileWriter verticesWriter = new FileWriter(verticesFile);
			verticesWriter.write(ConnectedComponentsData.getEnumeratingVertices(NUM_VERTICES));
			verticesWriter.close();

			FileWriter edgesWriter = new FileWriter(edgesFile);
			edgesWriter.write(ConnectedComponentsData.getRandomOddEvenEdges(NUM_EDGES, NUM_VERTICES, SEED));
			edgesWriter.close();

			int dop = 4;
			String verticesPath = "file://" + verticesFile.getAbsolutePath();
			String edgesPath = "file://" + edgesFile.getAbsolutePath();
			String resultPath = "file://" + resultFile.getAbsolutePath();
			int maxIterations = 100;

			String[] params = { String.valueOf(dop) , verticesPath, edgesPath, resultPath, String.valueOf(maxIterations) };

			WorksetConnectedComponents cc = new WorksetConnectedComponents();
			lde.start(2);
			lde.run(cc.getPlan(params));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} finally {
			try {
				lde.stop();
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
		}
	}
}
