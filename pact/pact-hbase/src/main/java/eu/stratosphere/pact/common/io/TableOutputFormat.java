//package eu.stratosphere.pact.common.io;
//
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.util.Map;
//import java.util.TreeMap;
//import java.util.UUID;
//
//import org.apache.hadoop.hbase.HConstants;
//import org.apache.hadoop.hbase.KeyValue;
//import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
//import org.apache.hadoop.hbase.io.hfile.Compression;
//import org.apache.hadoop.hbase.io.hfile.HFile;
//import org.apache.hadoop.hbase.regionserver.StoreFile;
//import org.apache.hadoop.hbase.regionserver.TimeRangeTracker;
//import org.apache.hadoop.hbase.util.Bytes;
//import org.apache.hadoop.io.WritableUtils;
//
//import eu.stratosphere.nephele.configuration.Configuration;
//import eu.stratosphere.nephele.fs.FileSystem;
//import eu.stratosphere.nephele.fs.Path;
//import eu.stratosphere.pact.common.generic.io.OutputFormat;
//import eu.stratosphere.pact.common.type.PactRecord;
//
//public class TableOutputFormat implements OutputFormat<PactRecord> {
//
//	  /*
//	   * Data structure to hold a Writer and amount of data written on it.
//	   */
//	static class WriterLength {
//	  long written = 0;
//	  HFile.Writer writer = null;
//	}
//	
//	/**
//	 * The key under which the name of the target path is stored in the configuration. 
//	 */
//	public static final String FILE_PARAMETER_KEY = "pact.output.file";
//	
//	public static final String HBASE_HREGION_MAX_FILESIZE = "hbase.hregion.max.filesize";
//	
//	public static final String HBASE_HFILEOUTPUTFORMAT_BLOCKSIZE = "hbase.mapreduce.hfileoutputformat.blocksize";
//	
//	public static final String HFILE_COMPRESSION = "hfile.compression";
//	
//	public static final String COMPRESSION_CONF_KEY = "hbase.hfileoutputformat.families.compression";
//	
//	public static final String HBASE_BULK_LOAD_TASK_KEY = "hbase.bulk.load.task.key";
//	
//	private Map<byte[], String> compressionMap;
//	
//	/**
//	 * The path of the file to be written.
//	 */
//	protected Path outputFilePath;
//	
//	
//	
//	// Map of families to writers and how much has been output on the writer.
//    private final Map<byte [], WriterLength> writers =
//      new TreeMap<byte [], WriterLength>(Bytes.BYTES_COMPARATOR);
//    private byte [] previousRow = HConstants.EMPTY_BYTE_ARRAY;
//    private final byte [] now = Bytes.toBytes(System.currentTimeMillis());
//    private boolean rollRequested = false;
//    private String defaultCompression;
//    
//    private long maxsize;
//    
//    private int blocksize;
//    
//    private FileSystem fs;
//    
//    private TimeRangeTracker trt;
//    
//    private String bulkLoadTaskKey;
//	
//	@Override
//	public void configure(Configuration parameters) {
//		
//		String filePath = parameters.getString(FILE_PARAMETER_KEY, null);
//		if (filePath == null) {
//			throw new IllegalArgumentException("Configuration file FileOutputFormat does not contain the file path.");
//		}
//		
//		try {
//			this.outputFilePath = new Path(filePath);
//		}
//		catch (RuntimeException rex) {
//			throw new RuntimeException("Could not create a valid URI from the given file path name: " + rex.getMessage()); 
//		}
//		
//		
//		// Get the path of the temporary output file
//	    //final Path outputdir = new FileOutputCommitter(outputFilePath, context).getWorkPath();
//	    try {
//			fs = outputFilePath.getFileSystem ();
//		} catch (IOException e) {
//			throw new RuntimeException("Could not retrieve filesystem from the given file path: " + e.getMessage());
//		}
//	    // These configs. are from hbase-*.xml
//	    
//	    maxsize = parameters.getLong(HBASE_HREGION_MAX_FILESIZE, HConstants.DEFAULT_MAX_FILE_SIZE);
//	    blocksize = parameters.getInteger(HBASE_HFILEOUTPUTFORMAT_BLOCKSIZE, HFile.DEFAULT_BLOCKSIZE);
//	    
//	    trt = new TimeRangeTracker();
//	    
//	    // Invented config.  Add to hbase-*.xml if other than default compression.
//	    defaultCompression = parameters.getString(HFILE_COMPRESSION, Compression.Algorithm.NONE.getName());
//
//	    // create a map from column family to the compression algorithm
//	    compressionMap = createFamilyCompressionMap(parameters);
//	    
//	    bulkLoadTaskKey = parameters.getString(HBASE_BULK_LOAD_TASK_KEY, null);
//	    if (bulkLoadTaskKey == null) {
//	    	throw new RuntimeException ("No HBase bulk load task key specified");
//	    }
//		
//	}
//
//	@Override
//	public void open(int taskNumber) throws IOException {
//		// TODO Auto-generated method stub
//		
//		// TODO add the suff that is done by configureIncrementalLoad
//		
//	}
//
//	@Override
//	public void writeRecord(PactRecord record) throws IOException
//	{
//		
//		
//		ImmutableBytesWritable row;
//		KeyValue kv;
//		
//		
//		// null input == user explicitly wants to flush
//        if (row == null && kv == null) {
//          rollWriters();
//          return;
//        }
//
//        byte [] rowKey = kv.getRow();
//        long length = kv.getLength();
//        byte [] family = kv.getFamily();
//        WriterLength wl = this.writers.get(family);
//
//        // If this is a new column family, verify that the directory exists
//        if (wl == null) {
//          fs.mkdirs(new Path(outputFilePath, Bytes.toString(family)));
//        }
//
//        // If any of the HFiles for the column families has reached
//        // maxsize, we need to roll all the writers
//        if (wl != null && wl.written + length >= maxsize) {
//          this.rollRequested = true;
//        }
//
//        // This can only happen once a row is finished though
//        if (rollRequested && Bytes.compareTo(this.previousRow, rowKey) != 0) {
//          rollWriters();
//        }
//
//        // create a new HLog writer, if necessary
//        if (wl == null || wl.writer == null) {
//          wl = getNewWriter(family);
//        }
//
//        // we now have the proper HLog writer. full steam ahead
//        kv.updateLatestStamp(this.now);
//        trt.includeTimestamp(kv);
//        wl.writer.append(kv);
//        wl.written += length;
//
//        // Copy the row so we know when a row transition.
//        this.previousRow = rowKey;
//		
//	}
//	
//	private void rollWriters() throws IOException {
//        for (WriterLength wl : this.writers.values()) {
//          if (wl.writer != null) {
//            LOG.info("Writer=" + wl.writer.getPath() +
//                ((wl.written == 0)? "": ", wrote=" + wl.written));
//            close(wl.writer);
//          }
//          wl.writer = null;
//          wl.written = 0;
//        }
//        this.rollRequested = false;
//      }
//
//      /* Create a new HFile.Writer.
//       * @param family
//       * @return A WriterLength, containing a new HFile.Writer.
//       * @throws IOException
//       */
//      private WriterLength getNewWriter(byte[] family)
//          throws IOException {
//        WriterLength wl = new WriterLength();
//        Path familydir = new Path(outputFilePath, Bytes.toString(family));
//        String compression = compressionMap.get(family);
//        compression = compression == null ?  defaultCompression : compression;
//        wl.writer =
//          HFile.getWriterFactory(conf).createWriter(fs, getRandomFileName(fs, familydir), blocksize, compression, KeyValue.KEY_COMPARATOR);
//        this.writers.put(family, wl);
//        return wl;
//      }
//      
//      private void close(final HFile.Writer w) throws IOException {
//          if (w != null) {
//            w.appendFileInfo(StoreFile.BULKLOAD_TIME_KEY, Bytes.toBytes(System.currentTimeMillis()));
//            w.appendFileInfo(StoreFile.BULKLOAD_TASK_KEY, Bytes.toBytes(bulkLoadTaskKey));
//            w.appendFileInfo(StoreFile.MAJOR_COMPACTION_KEY, Bytes.toBytes(true));
//            // TODO StoreFile.TIMERANGE_KEY is public in HBase 0.92.1 
//            //w.appendFileInfo(StoreFile.TIMERANGE_KEY, WritableUtils.toByteArray(trt));
//            w.appendFileInfo(Bytes.toBytes("TIMERANGE"), WritableUtils.toByteArray(trt));
//            w.close();
//          }
//        }
//
//	@Override
//	public void close() throws IOException {
//		for (WriterLength wl: this.writers.values()) {
//	        close(wl.writer);
//	     }
//		
//	}
//
//
//	 /**
//	   * Run inside the task to deserialize column family to compression algorithm
//	   * map from the
//	   * configuration.
//	   * 
//	   * Package-private for unit tests only.
//	   * 
//	   * @return a map from column family to the name of the configured compression
//	   *         algorithm
//	   */
//	  static Map<byte[], String> createFamilyCompressionMap(Configuration conf) {
//	    Map<byte[], String> compressionMap = new TreeMap<byte[], String>(Bytes.BYTES_COMPARATOR);
//	    String compressionConf = conf.getString(COMPRESSION_CONF_KEY, "");
//	    for (String familyConf : compressionConf.split("&")) {
//	      String[] familySplit = familyConf.split("=");
//	      if (familySplit.length != 2) {
//	        continue;
//	      }
//	      
//	      try {
//	        compressionMap.put(URLDecoder.decode(familySplit[0], "UTF-8").getBytes(),
//	            URLDecoder.decode(familySplit[1], "UTF-8"));
//	      } catch (UnsupportedEncodingException e) {
//	        // will not happen with UTF-8 encoding
//	        throw new AssertionError(e);
//	      }
//	    }
//	    return compressionMap;
//	  }
//	
//	  /**
//	   * Based on org.apache.hadoop.hbase.regionserver.StoreFIle#getUniqueFile(final FileSystem fs, final Path dir)
//	   * 
//	   * @param fs
//	   * @param dir
//	   * @return
//	   */
//	  private Path getRandomFileName (FileSystem fs, Path dir) {
//		  try {
//			  if (!fs.getFileStatus(dir).isDir()) {
//			      throw new RuntimeException("Expecting " + dir.toString() + " to be a directory");
//			  }
//		  } catch (IOException e) {
//			  throw new RuntimeException ("Error checking file status of " + dir + ", " + e.getMessage());
//		  }
//		  
//		  return new Path(dir, UUID.randomUUID().toString().replaceAll("-", ""));
//	  }
//}
