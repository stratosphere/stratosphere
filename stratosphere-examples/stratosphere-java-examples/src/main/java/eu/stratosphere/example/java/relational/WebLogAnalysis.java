/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
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

package eu.stratosphere.example.java.relational;

import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.CoGroupFunction;
import eu.stratosphere.api.java.functions.FlatMapFunction;
import eu.stratosphere.api.java.functions.JoinFunction;
import eu.stratosphere.api.java.tuple.Tuple1;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.util.Collector;

import java.util.Iterator;

/**
 * Implements the following relational OLAP query as PACT program:
 *
 * <code><pre>
 * SELECT r.pageURL, r.pageRank, r.avgDuration
 * FROM Documents d JOIN Rankings r
 * 	ON d.url = r.url
 * WHERE CONTAINS(d.text, [keywords])
 * 	AND r.rank > [rank]
 * 	AND NOT EXISTS (
 * 		SELECT * FROM Visits v
 * 		WHERE v.destUrl = d.url
 * 			AND v.visitDate < [date]);
 *  * </pre></code>
 *
 * Table Schemas: <code><pre>
 * CREATE TABLE Documents (
 * 					url VARCHAR(100) PRIMARY KEY,
 * 					contents TEXT );
 *
 * CREATE TABLE Rankings (
 * 					pageRank INT,
 * 					pageURL VARCHAR(100) PRIMARY KEY,
 * 					avgDuration INT );
 *
 * CREATE TABLE Visits (
 * 					sourceIP VARCHAR(16),
 * 					destURL VARCHAR(100),
 * 					visitDate DATE,
 * 					adRevenue FLOAT,
 * 					userAgent VARCHAR(64),
 * 					countryCode VARCHAR(3),
 * 					languageCode VARCHAR(6),
 * 					searchWord VARCHAR(32),
 * 					duration INT );
 * </pre></code>
 *
 */
public class WebLogAnalysis {

	/**
	 * MapFunction that filters for documents that contain a certain set of
	 * keywords.
	 */
	public static class FilterDocs extends FlatMapFunction<Tuple2<String, String>, Tuple1<String>> {

		private static final long serialVersionUID = 1L;

		private static final String[] KEYWORDS = { " editors ", " oscillations ", " convection " };

		/**
		 * Filters for documents that contain all of the given keywords and projects the records on the URL field.
		 *
		 * Output Format:
		 * 0: URL
		 */
		@Override
		public void flatMap(Tuple2<String, String> value, Collector<Tuple1<String>> out) throws Exception {
			// FILTER
			// Only collect the document if all keywords are contained
			String docText = value.f1;
			boolean allContained = true;
			for (String kw : KEYWORDS) {
				if (!docText.contains(kw)) {
					allContained = false;
					break;
				}
			}

			if (allContained) {
				out.collect(new Tuple1(value.f0));
			}
		}
	}

	/**
	 * MapFunction that filters for records where the rank exceeds a certain threshold.
	 */
	public static class FilterRanks extends FlatMapFunction<Tuple3<Integer, String, Integer>, Tuple3<Integer, String, Integer>> {

		private static final long serialVersionUID = 1L;

		private static final int RANKFILTER = 50;

		/**
		 * Filters for records of the rank relation where the rank is greater
		 * than the given threshold.
		 *
		 * Output Format:
		 * 0: RANK
		 * 1: URL
		 * 2: AVG_DURATION
		 */
		@Override
		public void flatMap(Tuple3<Integer, String, Integer> value, Collector<Tuple3<Integer, String, Integer>> out) throws Exception {

			if (value.f0 > RANKFILTER) {
				out.collect(value);
			}
		}
	}

	/**
	 * MapFunction that filters for records of the visits relation where the year
	 * (from the date string) is equal to a certain value.
	 */
	public static class FilterVisits extends FlatMapFunction<Tuple2<String, String>, Tuple1<String>> {

		private static final long serialVersionUID = 1L;

		private static final int YEARFILTER = 2010;

		/**
		 * Filters for records of the visits relation where the year of visit is equal to a
		 * specified value. The URL of all visit records passing the filter is emitted.
		 *
		 * Output Format:
		 * 0: URL
		 */
		@Override
		public void flatMap(Tuple2<String, String> value, Collector<Tuple1<String>> out) throws Exception {
			// Parse date string with the format YYYY-MM-DD and extract the year
			String dateString = value.f1;
			int year = Integer.parseInt(dateString.substring(0,4));

			if (year == YEARFILTER) {
				out.collect(new Tuple1(value.f0));
			}
		}
	}

	/**
	 * JoinFunction that joins the filtered entries from the documents and the
	 * ranks relation.
	 */
	public static class JoinDocRanks extends JoinFunction<Tuple1<String>, Tuple3<Integer, String, Integer>, Tuple3<Integer, String, Integer>>{
		private static final long serialVersionUID = 1L;

		/**
		 * Joins entries from the documents and ranks relation on their URL.
		 *
		 * Output Format:
		 * 0: RANK
		 * 1: URL
		 * 2: AVG_DURATION
		 */
		@Override
		public Tuple3<Integer, String, Integer> join(Tuple1<String> document, Tuple3<Integer, String, Integer> rank) throws Exception {
			return rank;
		}
	}

	/**
	 * CoGroupFunction that realizes an anti-join.
	 * If the first input does not provide any pairs, all pairs of the second input are emitted.
	 * Otherwise, no pair is emitted.
	 */
	public static class AntiJoinVisits extends CoGroupFunction<Tuple3<Integer, String, Integer>, Tuple1<String>, Tuple3<Integer, String, Integer>> {
		private static final long serialVersionUID = 1L;

		/**
		 * If the visit iterator is empty, all pairs of the rank iterator are emitted.
		 * Otherwise, no pair is emitted.
		 *
		 * Output Format:
		 * 0: RANK
		 * 1: URL
		 * 2: AVG_DURATION
		 */
		@Override
		public void coGroup(Iterator<Tuple3<Integer, String, Integer>> ranks, Iterator<Tuple1<String>> visits, Collector<Tuple3<Integer, String, Integer>> out) {
			// Check if there is a entry in the visits relation
			if (!visits.hasNext()) {
				while (ranks.hasNext()) {
					// Emit all rank pairs
					out.collect(ranks.next());
				}
			}
		}

		@Override
		public void combineFirst(Iterator<Tuple3<Integer, String, Integer>> records, Collector<Tuple3<Integer, String, Integer>> out) throws Exception {
		}

		@Override
		public void combineSecond(Iterator<Tuple1<String>> records, Collector<Tuple1<String>> out) throws Exception {
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.err.println("Usage: WebLogAnalysis <docs> <ranks> <visits> <output>");
			return;
		}
		String docsInput   = args[0];
		String ranksInput  = args[1];
		String visitsInput = args[2];
		String output      = args[3];

		final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

		/*
		 * Output Format:
		 * 0: URL
		 * 1: DOCUMENT_TEXT
		 */
		// Create DataSet for documents relation
		DataSet<Tuple2<String, String>> docs = env.readCsvFile(docsInput)
			.fieldDelimiter('|')
			.types(String.class, String.class);

		/*
		 * Output Format:
		 * 0: RANK
		 * 1: URL
		 * 2: AVG_DURATION
		 */
		// Create DataSet for ranks relation
		DataSet<Tuple3<Integer, String, Integer>> ranks = env.readCsvFile(ranksInput)
			.fieldDelimiter('|')
			.types(Integer.class, String.class, Integer.class);

		/*
		 * Output Format:
		 * 0: URL
		 * 1: DATE
		 */
		// Create DataSet for visits relation
		DataSet<Tuple2<String, String>> visits = env.readCsvFile(visitsInput)
			.fieldDelimiter('|')
			.includeFields("011000000")
			.types(String.class, String.class);

		// Create MapOperator for filtering the entries from the documents relation
		DataSet<Tuple1<String>> filterDocs = docs.flatMap(new FilterDocs());

		// Create MapOperator for filtering the entries from the ranks relation
		DataSet<Tuple3<Integer, String, Integer>> filterRanks = ranks.flatMap(new FilterRanks());

		// Create MapOperator for filtering the entries from the visits relation
		DataSet<Tuple1<String>> filterVisits = visits.flatMap(new FilterVisits());

		// Create JoinOperator to join the filtered documents and ranks relation
		DataSet<Tuple3<Integer, String, Integer>> joinDocsRanks = filterDocs.join(filterRanks)
			.where(0).equalTo(1).with(new JoinDocRanks());

		// Create CoGroupOperator to realize a anti join between the joined
		// documents and ranks relation and the filtered visits relation
		DataSet<Tuple3<Integer, String, Integer>> antiJoinVisits = joinDocsRanks.coGroup(filterVisits)
			.where(1).equalTo(0).with(new AntiJoinVisits());

		if (output.equals("STDOUT")) {
			antiJoinVisits.print();
		} else {
			antiJoinVisits.writeAsCsv(output, "\n", "|");
		}

		env.execute();
	}
}
