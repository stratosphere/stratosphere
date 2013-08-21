package eu.stratosphere.pact.common.io.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapred.TableRecordReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import eu.stratosphere.pact.common.io.HBaseKey;
import eu.stratosphere.pact.common.io.HBaseResult;
import eu.stratosphere.pact.common.io.TableInputFormat;
import eu.stratosphere.pact.common.io.TableInputSplit;

/**
 * Integration test for the {@link TableInputFormat} class which wraps the 
 * access to Hbase tables.
 * 
 * @author Mathias Peters <mathias.peters@informatik.hu-berlin.de>
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TableInputFormat.class})
public class HBaseInputFormatTestIT {
	
	private static final String TEST_TABLE = "test";

	eu.stratosphere.nephele.configuration.Configuration configuration;
	
	@Mock
	private TableInputSplit splitMock;
	
	@Mock
	private Scan scanMock;

	private byte[] startRow = new byte[]{};
	private byte[] endRow = new byte[]{};
	
	@Mock
	private HTable tableMock;
	
	@Mock
	private TableRecordReader recordReaderMock;
	
	@Mock
	private HBaseConfiguration hbaseConfigMock;
	
	private HBaseKey hbaseKey;
	private HBaseResult hbaseResult;
	
	@Before
	public void setUp() throws Exception
	{
		configuration = new eu.stratosphere.nephele.configuration.Configuration();
		configuration.setString(TableInputFormat.INPUT_TABLE, TEST_TABLE);
		configuration.setString(TableInputFormat.CONFIG_LOCATION, "src/test/resources/hbase-site.xml");
		
		this.hbaseKey = new HBaseKey();
		this.hbaseResult = new HBaseResult();
		
		initMocks(this);
		when(splitMock.getStartRow()).thenReturn(startRow);
		when(splitMock.getEndRow()).thenReturn(endRow);
		whenNew(HTable.class).withArguments(Mockito.anyString()).thenReturn(this.tableMock);
		whenNew(Scan.class).withNoArguments().thenReturn(this.scanMock);
		whenNew(Scan.class).withArguments(this.scanMock).thenReturn(this.scanMock);
		whenNew(TableRecordReader.class).withNoArguments().thenReturn(this.recordReaderMock);
	}
	
	@Test
	public void shouldOpenFormat() throws IOException
	{
		TableInputFormat out = new TableInputFormat();
		out.setTable(this.tableMock);
		out.setScan(this.scanMock);
		out.open(this.splitMock);
		
		verify(this.scanMock).setStartRow(startRow);
		verify(this.scanMock).setStopRow(endRow);
		verify(this.recordReaderMock).init();
	}

}
