/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.nephele.visualization.swt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;

import eu.stratosphere.nephele.event.job.IterationTimeSeriesEvent;
import eu.stratosphere.nephele.profiling.types.InstanceProfilingEvent;

public class InstanceVisualizationData {

	private final static long BYTE_TO_MEGABIT = 1024 * 1024 / 8;

	private final static long KILOBYTE_TO_MEGABYTE = 1024;

	private final DefaultTableXYDataset cpuDataSet;

//	private final DefaultTableXYDataset memoryDataSet;

	private final DefaultTableXYDataset networkDataSet;
	

	// Series for CPU data
	private final XYSeries cpuUsrSeries;

	private final XYSeries cpuSysSeries;

	private final XYSeries cpuWaitSeries;

	private final XYSeries cpuHardIrqSeries;

	private final XYSeries cpuSoftIrqSeries;

	// Series for network data
	private final XYSeries networkReceivedSeries;

	private final XYSeries networkTransmittedSeries;

//	// Series for memory data
//	private final XYSeries totalMemorySeries;
//
//	private final XYSeries usedMemorySeries;
//
//	private final XYSeries cachedMemorySeries;

	// Iteration status 
	 //TODO  @micha add stuff for iteration time series
	private List<String> iterationMetrics;	

	// data of iteration status
	private Map<String,TableXYDataset> iterationDatasets;

	// Series for iteration status datas
	private Map<String,XYSeries> iterationStatusSeries;

	// to save max values for charts because AutoRange is broken
	private Map<String,Double> upperBoundsForIterCharts;
	
	private final boolean isProfilingAvailable;

	private long totalMemoryinMB = 1024;
	
	// toggles display of status charts
	private boolean isIterationAvailable;

	public InstanceVisualizationData(boolean isProfilingAvailable){

		this.isProfilingAvailable = isProfilingAvailable;

		this.cpuDataSet = new DefaultTableXYDataset();
//		this.memoryDataSet = new DefaultTableXYDataset();
		this.networkDataSet = new DefaultTableXYDataset();

		this.cpuUsrSeries = new XYSeries("USR", false, false);
		this.cpuUsrSeries.setNotify(false);
		this.cpuSysSeries = new XYSeries("SYS", false, false);
		this.cpuSysSeries.setNotify(false);
		this.cpuWaitSeries = new XYSeries("WAIT", false, false);
		this.cpuWaitSeries.setNotify(false);
		this.cpuHardIrqSeries = new XYSeries("HI", false, false);
		this.cpuHardIrqSeries.setNotify(false);
		this.cpuSoftIrqSeries = new XYSeries("SI", false, false);
		this.cpuSoftIrqSeries.setNotify(false);

		// Initialize data sets
		this.cpuDataSet.addSeries(this.cpuWaitSeries);
		this.cpuDataSet.addSeries(this.cpuSysSeries);
		this.cpuDataSet.addSeries(this.cpuUsrSeries);
		this.cpuDataSet.addSeries(this.cpuHardIrqSeries);
		this.cpuDataSet.addSeries(this.cpuSoftIrqSeries);

		this.networkReceivedSeries = new XYSeries("Received", false, false);
		this.networkReceivedSeries.setNotify(false);
		this.networkTransmittedSeries = new XYSeries("Transmitted", false, false);
		this.networkTransmittedSeries.setNotify(false);

		this.networkDataSet.addSeries(this.networkReceivedSeries);
		this.networkDataSet.addSeries(this.networkTransmittedSeries);

//		this.totalMemorySeries = new XYSeries("Total", false, false);
//		this.totalMemorySeries.setNotify(false);
//		this.usedMemorySeries = new XYSeries("Used", false, false);
//		this.usedMemorySeries.setNotify(false);
//		this.cachedMemorySeries = new XYSeries("Cached", false, false);
//		this.cachedMemorySeries.setNotify(false);
//
//		// We do not add the total memory to the collection				
//		this.memoryDataSet.addSeries(this.cachedMemorySeries);
//		this.memoryDataSet.addSeries(this.usedMemorySeries);

		
	
	}
	
	/**
	 * Constructor of visualization data of whole job with iteration status data
	 * 
	 * @param isProfilingAvailable
	 * @param iterationMetrics names of the iteration metrics for the charts
	 */
	public InstanceVisualizationData(boolean isProfilingAvailable, List<String> iterationMetrics){

	this(isProfilingAvailable);
	
	// check if there are iteration metrics to later toggle display of status charts
	this.isIterationAvailable = !iterationMetrics.isEmpty();
	
	this.iterationMetrics = iterationMetrics;
	// Initialize maps for iteration data of different metrics
	iterationDatasets = new HashMap<String, TableXYDataset>();
	iterationStatusSeries = new HashMap<String, XYSeries>();
	upperBoundsForIterCharts = new HashMap<String, Double>();
	
	// Initialize datasets for all iteration metrics
	Iterator<String> nameIt = iterationMetrics.iterator();
	while (nameIt.hasNext()){
		String iterationMetricName = nameIt.next();
		// initialize iteration status data sets
		
		DefaultTableXYDataset iterationData = new DefaultTableXYDataset();
		
		XYSeries iterationStatusSeriesOne = new XYSeries(iterationMetricName, false, false);
		iterationStatusSeriesOne.setNotify(false);
		
		// connect table data with data series
		iterationData.addSeries(iterationStatusSeriesOne);
		
		// set both in maps with iteration metric name
		this.iterationDatasets.put(iterationMetricName, iterationData);			 
		this.iterationStatusSeries.put(iterationMetricName, iterationStatusSeriesOne);
		
		// set initial upper bound to 0.0
		this.upperBoundsForIterCharts.put(iterationMetricName,0.0);
	}
	}

	public TableXYDataset getCpuDataSet() {
		return this.cpuDataSet;
	}

//	public TableXYDataset getMemoryDataSet() {
//		return this.memoryDataSet;
//	}

	public TableXYDataset getNetworkDataSet() {
		return this.networkDataSet;
	}
	
	/**
	 * Get names of iterations metrics
	 * @return names of iteration status metrics
	 */
	public List<String> getIterationMetrics() {
		return iterationMetrics;
	}
	
	public TableXYDataset getIterationDataSet(String metricName) {
		return this.iterationDatasets.get(metricName);
	}

	public double getUpperBoundForMemoryChart() {
		return ((double) this.totalMemoryinMB) * 1.05;
	}
	
	public double getUpperBoundForIterChart(String metricName) {
		return this.upperBoundsForIterCharts.get(metricName);
	}

	public void processInstanceProfilingEvent(InstanceProfilingEvent instanceProfilingEvent) {

		double timestamp = VertexVisualizationData.getTimestamp(instanceProfilingEvent);

//		final long instanceMemoryInMB = instanceProfilingEvent.getTotalMemory() / KILOBYTE_TO_MEGABYTE;
//		if (instanceMemoryInMB > this.totalMemoryinMB) {
//			this.totalMemoryinMB = instanceMemoryInMB;
//		}
//
//		final long cachedMemory = instanceProfilingEvent.getBufferedMemory() + instanceProfilingEvent.getCachedMemory()
//			+ instanceProfilingEvent.getCachedSwapMemory();
//
//		final long usedMemory = instanceProfilingEvent.getTotalMemory() - instanceProfilingEvent.getFreeMemory()
//			- cachedMemory;

		this.cpuUsrSeries.addOrUpdate(timestamp, instanceProfilingEvent.getUserCPU());
		this.cpuSysSeries.addOrUpdate(timestamp, instanceProfilingEvent.getSystemCPU());
		this.cpuWaitSeries.addOrUpdate(timestamp, instanceProfilingEvent.getIOWaitCPU());
		this.cpuHardIrqSeries.addOrUpdate(timestamp, instanceProfilingEvent.getHardIrqCPU());
		this.cpuSoftIrqSeries.addOrUpdate(timestamp, instanceProfilingEvent.getSoftIrqCPU());
//		this.totalMemorySeries.addOrUpdate(timestamp, instanceProfilingEvent.getTotalMemory() / KILOBYTE_TO_MEGABYTE);
//		this.usedMemorySeries.addOrUpdate(timestamp, usedMemory / KILOBYTE_TO_MEGABYTE);
//		this.cachedMemorySeries.addOrUpdate(timestamp, cachedMemory / KILOBYTE_TO_MEGABYTE);
		this.networkReceivedSeries.addOrUpdate(timestamp, toMBitPerSec(instanceProfilingEvent.getReceivedBytes(),
			instanceProfilingEvent.getProfilingInterval()));
		this.networkTransmittedSeries.addOrUpdate(timestamp, toMBitPerSec(instanceProfilingEvent.getTransmittedBytes(),
			instanceProfilingEvent.getProfilingInterval()));
	}
	
	//  TODO @micha add data to right series by name
	public void processIterationTimeSeriesEvent(IterationTimeSeriesEvent iterationEvent) {
		// add data to right series by name
		this.iterationStatusSeries.get(iterationEvent.getSeriesName()).addOrUpdate(iterationEvent.getTimeStep(), iterationEvent.getValue());
		// update upper bound for chart if necessary
		if (iterationEvent.getValue() > this.upperBoundsForIterCharts.get(iterationEvent.getSeriesName())){
			this.upperBoundsForIterCharts.put(iterationEvent.getSeriesName(),iterationEvent.getValue());
		}
	}

	@SuppressWarnings("unchecked")
	public double getAverageUserTime() {

		double av = 0.0f;

		if (this.cpuUsrSeries != null) {
			av = calculateAverage(this.cpuUsrSeries.getItems());
		}

		return av;
	}

	private double calculateAverage(List<XYDataItem> list) {

		double av = 0.0f;

		Iterator<XYDataItem> it = list.iterator();
		while (it.hasNext()) {
			av += it.next().getYValue();
		}

		return (av / (double) list.size());
	}

	private final double toMBitPerSec(long numberOfBytes, long profilingPeriod) {

		return (((double) numberOfBytes) / ((double) (BYTE_TO_MEGABIT * profilingPeriod / 1000L)));
	}

	public boolean isProfilingEnabledForJob() {
		return this.isProfilingAvailable;
	}
	
	public boolean isIterationJob() {
		return this.isIterationAvailable;
	}


}
