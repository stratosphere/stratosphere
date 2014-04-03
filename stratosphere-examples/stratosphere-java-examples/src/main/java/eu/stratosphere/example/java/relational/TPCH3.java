package eu.stratosphere.example.java.TPCH3;

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
import eu.stratosphere.api.java.tuple.Tuple7;

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
	public static class Lineitem {

		public Integer l_orderkey;
		public Double l_discount;
		public Double l_extendedprice;
		public String l_shipdate;

		public Lineitem() {
			// default constructor
		}

		public Lineitem(Integer l_orderkey, Double l_extendedprice,
				Double l_discount, String l_shipdate) {
			this.l_orderkey = l_orderkey;
			this.l_discount = l_discount;
			this.l_extendedprice = l_extendedprice;
			this.l_shipdate = l_shipdate;
		}
	}

	public static class Customer {

		public Integer c_custkey;
		public String c_mktsegment;

		public Customer() {
			// default constructor
		}

		public Customer(Integer c_custkey, String c_mktsegment) {
			this.c_custkey = c_custkey;
			this.c_mktsegment = c_mktsegment;
		}
	}

	public static class Order {

		public Integer o_orderkey;
		public String o_orderdate;
		public Integer o_shippriority;

		public Order() {
			// default constructor
		}

		public Order(Integer o_orderkey, String o_orderdate,
				Integer o_shippriority) {
			this.o_orderkey = o_orderkey;
			this.o_orderdate = o_orderdate;
			this.o_shippriority = o_shippriority;
		}
	}

	public static class ShippingPriorityItem {

		public Integer l_orderkey;
		public Double revenue;
		public String o_orderdate;
		public Integer o_shippriority;
		public Integer o_orderkey;

		public ShippingPriorityItem() {
			// default constructor
		}

		public ShippingPriorityItem(Integer l_orderkey, Double revenue,
				String o_orderdate, Integer o_shippriority, Integer o_orderkey) {
			this.l_orderkey = l_orderkey;
			this.revenue = revenue;
			this.o_orderdate = o_orderdate;
			this.o_shippriority = o_shippriority;
			this.o_orderkey = o_orderkey;
		}
		
		@Override
		public String toString() {
			return l_orderkey + "; " + revenue + "; " + o_orderdate + "; " + o_shippriority + "; " + o_orderkey;
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
				
				return value.c_mktsegment.equals("AUTOMOBILE");
			}
		});

		/*
		 * Filter all Orders with o_orderdate < 12.03.1995
		 */
		or = or.filter(new FilterFunction<Order>() {
			private static final long serialVersionUID = 1L;
			private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
			@Override
			public boolean filter(Order value) throws Exception {
				Calendar cal = Calendar.getInstance();
				cal.set(1995, 3, 12);
				Date orderDate = format.parse(value.o_orderdate);
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
				Date shipDate = format.parse(value.l_shipdate);
				return shipDate.after(cal.getTime());
			}
		});

		/*
		 * Join customers with orders and package them into a ShippingPriorityItem
		 */
		DataSet<ShippingPriorityItem> customerWithOrders = cust
				.join(or)
				.where(new KeySelector<Customer, Integer>() {

					private static final long serialVersionUID = 1L;

					@Override
					public Integer getKey(Customer value) {
						return value.c_custkey;
					}
				})
				.equalTo(new KeySelector<Order, Integer>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Integer getKey(Order value) {
						return value.o_orderkey;
					}
				})
				.with(new JoinFunction<Customer, Order, ShippingPriorityItem>() {
					private static final long serialVersionUID = 1L;

					@Override
					public ShippingPriorityItem join(Customer first,
							Order second) throws Exception {
						return new ShippingPriorityItem(0, 0.0,
								second.o_orderdate, second.o_shippriority,
								second.o_orderkey);
					}
				});
		
		/*
		 * Join the last join result with Orders
		 */
		DataSet<ShippingPriorityItem> joined = customerWithOrders
				.join(li)
				.where(new KeySelector<ShippingPriorityItem, Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Integer getKey(ShippingPriorityItem value) {
						return value.o_orderkey;
					}

				})
				.equalTo(new KeySelector<Lineitem, Integer>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public Integer getKey(Lineitem value) {
						return value.l_orderkey;
					}
				})
				.with(new JoinFunction<ShippingPriorityItem, Lineitem, ShippingPriorityItem>() {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public ShippingPriorityItem join(
							ShippingPriorityItem first, Lineitem second)
							throws Exception {
						
						first.l_orderkey = second.l_orderkey;
						first.revenue = second.l_extendedprice * (1 - second.l_discount);
						return first;
					}
				});
		
		/*
		 * GroupBy l_orderkey, o_orderdate and o_shippriority
		 * After that, the reduce function calculates the revenue.
		 */
		joined = joined.groupBy(new KeySelector<ShippingPriorityItem, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getKey(ShippingPriorityItem value) {
				return value.l_orderkey.toString().concat(value.o_orderdate).concat(value.o_shippriority.toString());
			}
		}).reduce(new ReduceFunction<TPCH3.ShippingPriorityItem>() {
			private static final long serialVersionUID = 1L;

			@Override
			public ShippingPriorityItem reduce(ShippingPriorityItem value1,
					ShippingPriorityItem value2) throws Exception {
				value1.revenue += value2.revenue;
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
