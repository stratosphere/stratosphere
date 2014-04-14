/***********************************************************************************************************************
 *
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
 *
 **********************************************************************************************************************/

package eu.stratosphere.example.java.relational;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import eu.stratosphere.api.common.operators.Ordering;
import eu.stratosphere.api.java.DataSet;
import eu.stratosphere.api.java.ExecutionEnvironment;
import eu.stratosphere.api.java.functions.FilterFunction;
import eu.stratosphere.api.java.functions.JoinFunction;
import eu.stratosphere.api.java.functions.KeySelector;
import eu.stratosphere.api.java.functions.MapFunction;
import eu.stratosphere.api.java.functions.ReduceFunction;
import eu.stratosphere.api.java.tuple.Tuple2;
import eu.stratosphere.api.java.tuple.Tuple3;
import eu.stratosphere.api.java.tuple.Tuple4;
import eu.stratosphere.api.java.tuple.Tuple5;
import eu.stratosphere.api.java.tuple.Tuple7;
import eu.stratosphere.configuration.Configuration;

/**
 * This program implements a modified version of the TPC-H query 3. Order by ist not yet supported.
 * 
 * The original query can be found at
 * http://www.tpc.org/tpch/spec/tpch2.16.0.pdf (page 29).
 * 
 * This program implements the following SQL equivalent:
 * 
 * select l_orderkey, 
 * 		  sum(l_extendedprice*(1-l_discount)) as revenue,
 * 		  o_orderdate, 
 *        o_shippriority from customer, 
 *        orders, 
 *        lineitem 
 * where
 * 		  c_mktsegment = '[SEGMENT]' and 
 * 		  c_custkey = o_custkey and 
 * 		  l_orderkey = o_orderkey and
 *        o_orderdate < date '[DATE]' and 
 *        l_shipdate > date '[DATE]'
 * group by 
 * 		  l_orderkey, 
 * 	      o_orderdate, 
 *        o_shippriority 
 * order by  					//not yet
 * 		  revenue desc,
 *        o_orderdate;
 * 
 */
public class TPCH3 {
	public static class Lineitem extends Tuple4<Integer, Double, Double, String> {

		public Lineitem() {
			// default constructor
		}

		public Lineitem(Integer l_orderkey, Double l_extendedprice,
				Double l_discount, String l_shipdate) {
			this.f0 = l_orderkey;
			this.f1 = l_extendedprice;
			this.f2 = l_discount;
			this.f3 = l_shipdate;
		}

		public Integer getOrderkey() {
			return this.f0;
		}

		public void setOrderkey(Integer l_orderkey) {
			this.f0 = l_orderkey;
		}

		public Double getDiscount() {
			return this.f2;
		}

		public void setDiscount(Double l_discount) {
			this.f2 = l_discount;
		}

		public Double getExtendedprice() {
			return this.f1;
		}

		public void setExtendedprice(Double l_extendedprice) {
			this.f1 = l_extendedprice;
		}

		public String getShipdate() {
			return this.f3;
		}

		public void setShipdate(String l_shipdate) {
			this.f3 = l_shipdate;
		}
	}

	public static class Customer extends Tuple2<Integer, String>{

		public Customer() {
		}

		public Customer(Integer c_custkey, String c_mktsegment) {
			this.f0 = c_custkey;
			this.f1 = c_mktsegment;
		}
		
		public Integer getCustKey() {
			return this.f0;
		}
		
		public String getMktsegment() {
			return this.f1;
		}
		
		public void setCustKey(Integer c_custkey) {
			this.f0 = c_custkey;
		}
		
		public void setMktsegment(String c_mktsegment) {
			this.f1 = c_mktsegment;
		}
	}

	public static class Order extends Tuple3<Integer, String, Integer>{

		public Order() {
			// default constructor
		}

		public Order(Integer o_orderkey, String o_orderdate,
				Integer o_shippriority) {
			this.f0 = o_orderkey;
			this.f1 = o_orderdate;
			this.f2 = o_shippriority;
		}
		
		public Integer getOrderkey() {
			return this.f0;
		}

		public void setOrderkey(Integer o_orderkey) {
			this.f0 = o_orderkey;
		}

		public String getOrderdate() {
			return this.f1;
		}

		public void setOrderdate(String o_orderdate) {
			this.f1 = o_orderdate;
		}

		public Integer getShippriority() {
			return this.f2;
		}

		public void setShippriority(Integer o_shippriority) {
			this.f2 = o_shippriority;
		}
	}

	public static class ShippingPriorityItem extends Tuple5<Integer, Double, String, Integer, Integer> {

		public ShippingPriorityItem() {
			// default constructor
		}

		public ShippingPriorityItem(Integer l_orderkey, Double revenue,
				String o_orderdate, Integer o_shippriority, Integer o_orderkey) {
			this.f0 = l_orderkey;
			this.f1 = revenue;
			this.f2 = o_orderdate;
			this.f3 = o_shippriority;
			this.f4 = o_orderkey;
		}
		
		public Integer getL_Orderkey() {
			return this.f0;
		}

		public void setOrderkey(Integer l_orderkey) {
			this.f0 = l_orderkey;
		}

		public Double getRevenue() {
			return this.f1;
		}

		public void setRevenue(Double revenue) {
			this.f1 = revenue;
		}

		public String getOrderdate() {
			return this.f2;
		}

		public void setOrderdate(String o_orderdate) {
			this.f2 = o_orderdate;
		}

		public Integer getShippriority() {
			return this.f3;
		}

		public void setShippriority(Integer o_shippriority) {
			this.f3 = o_shippriority;
		}

		public Integer getO_Orderkey() {
			return this.f4;
		}

		public void setO_Orderkey(Integer o_orderkey) {
			this.f4 = o_orderkey;
		}

		@Override
		public String toString() {
			return this.f0 + "; " + this.f1 + "; " + this.f2 + "; " + this.f3 + "; " + this.f4;
		}
	}
	/*
	 * This example TPCH3 query uses custom objects.
	 */
	public static void main(String[] args) {
		final String lineitemPath;
		final String customerPath;
		final String ordersPath;

		if (args.length < 3) {
			throw new IllegalArgumentException(
					"Invalid number of parameters: [lineitem.tbl] [customer.tbl] [orders.tbl]");
		} else {
			lineitemPath = args[0];
			customerPath = args[1];
			ordersPath = args[2];
		}

		final ExecutionEnvironment env = ExecutionEnvironment
				.getExecutionEnvironment();
		env.setDegreeOfParallelism(1);

		/*
		 * Read Data from files
		 */
		DataSet<Tuple4<Integer, Double, Double, String>> lineitems = env
				.readCsvFile(lineitemPath).fieldDelimiter('|')
				.includeFields("1000011000100000")
				.types(Integer.class, Double.class, Double.class, String.class);
		DataSet<Tuple2<Integer, String>> customers = env
				.readCsvFile(customerPath).fieldDelimiter('|')
				.includeFields("10000010").types(Integer.class, String.class);
		DataSet<Tuple3<Integer, String, Integer>> orders = env
				.readCsvFile(ordersPath).fieldDelimiter('|')
				.includeFields("100010010")
				.types(Integer.class, String.class, Integer.class);

		/*
		 * Convert Tuple DataSet to Lineitem Dataset
		 */
		DataSet<Lineitem> li = lineitems
				.map(new MapFunction<Tuple4<Integer, Double, Double, String>, Lineitem>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Lineitem map(
							Tuple4<Integer, Double, Double, String> value)
							throws Exception {
						return new Lineitem(value.f0, value.f1, value.f2,
								value.f3);
					}
				});

		/*
		 * Convert Tuple DataSet to Order Dataset
		 */
		DataSet<Order> or;
			or = orders
					.map(new MapFunction<Tuple3<Integer, String, Integer>, Order>() {
						private static final long serialVersionUID = 1L;

						@Override
						public Order map(Tuple3<Integer, String, Integer> value)
								throws Exception {
							return new Order(value.f0, value.f1, value.f2);
						}
					});
	
		/*
		 * Convert Tuple DataSet to Customer Dataset
		 */
		DataSet<Customer> cust = customers
				.map(new MapFunction<Tuple2<Integer, String>, Customer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Customer map(Tuple2<Integer, String> value)
							throws Exception {
						return new Customer(value.f0, value.f1);
					}
				});
		
		/*
		 * Filter market segment "AUTOMOBILE"
		 */
		cust = cust.filter(new FilterFunction<Customer>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean filter(Customer value) throws Exception {
				
				return value.getMktsegment().equals("AUTOMOBILE");
			}
		});

		/*
		 * Filter all Orders with o_orderdate < 12.03.1995
		 */
		or = or.filter(new FilterFunction<Order>() {
			private static final long serialVersionUID = 1L;
			private  DateFormat format;
			
			@Override
			public void open(Configuration parameters) throws Exception {
				super.open(parameters);
				format = new SimpleDateFormat("yyyy-MM-dd");
			}
			
			@Override
			public boolean filter(Order value) throws Exception {
				Calendar cal = Calendar.getInstance();
				cal.set(1995, 3, 12);
				Date orderDate = format.parse(value.getOrderdate());
				return orderDate.before(cal.getTime());
			}
		});
		
		/*
		 * Filter all Lineitems with l_shipdate > 12.03.1995
		 */
		li = li.filter(new FilterFunction<Lineitem>() {
			private static final long serialVersionUID = 1L;
			private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			@Override
			public boolean filter(Lineitem value) throws Exception {
				Calendar cal = Calendar.getInstance();
				cal.set(1995, 3, 12);
				Date shipDate = format.parse(value.getShipdate());
				return shipDate.after(cal.getTime());
			}
		});

		/*
		 * Join customers with orders and package them into a ShippingPriorityItem
		 */
		DataSet<ShippingPriorityItem> customerWithOrders = cust
				.join(or)
				.where(0)
				.equalTo(0)
				.with(new JoinFunction<Customer, Order, ShippingPriorityItem>() {
					private static final long serialVersionUID = 1L;

					@Override
					public ShippingPriorityItem join(Customer first,
							Order second) throws Exception {
						ShippingPriorityItem it = new ShippingPriorityItem(0, 0.0,
								second.getOrderdate(), second.getShippriority(),
								second.getOrderkey());
						return it;
					}
				});
		
		/*
		 * Join the last join result with Orders
		 */
		DataSet<ShippingPriorityItem> joined = customerWithOrders
				.join(li)
				.where(4)
				.equalTo(0)
				.with(new JoinFunction<ShippingPriorityItem, Lineitem, ShippingPriorityItem>() {

					private static final long serialVersionUID = 1L;

					@Override
					public ShippingPriorityItem join(
							ShippingPriorityItem first, Lineitem second)
							throws Exception {
						
						first.setOrderkey(second.getOrderkey());
						first.setRevenue(second.getExtendedprice() * (1 - second.getDiscount()));
						return first;
					}
				});
		
		/*
		 * GroupBy l_orderkey, o_orderdate and o_shippriority
		 * After that, the reduce function calculates the revenue.
		 */
		joined = joined
				.groupBy(0, 2, 3)
				.reduce(new ReduceFunction<TPCH3.ShippingPriorityItem>() {
				private static final long serialVersionUID = 1L;
	
					@Override
					public ShippingPriorityItem reduce(ShippingPriorityItem value1,
							ShippingPriorityItem value2) throws Exception {
						value1.setRevenue(value1.getRevenue() + value2.getRevenue());
						return value1;
					}
			});
		
			try {
				joined.print();
				env.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
	
	}
	
	
}
