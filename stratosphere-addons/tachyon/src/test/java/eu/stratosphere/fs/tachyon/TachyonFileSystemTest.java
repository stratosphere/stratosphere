package eu.stratosphere.fs.tachyon;

import eu.stratosphere.example.java.record.wordcount.WordCount;
import java.net.InetAddress;
import org.junit.AfterClass;
import eu.stratosphere.fs.tachyon.util.LocalTachyonCluster;
import eu.stratosphere.core.fs.BlockLocation;
import eu.stratosphere.core.fs.FSDataInputStream;
import eu.stratosphere.core.fs.FSDataOutputStream;
import eu.stratosphere.core.fs.FileStatus;
import eu.stratosphere.core.fs.FileSystem;
import eu.stratosphere.core.fs.Path;
import java.io.IOException;
import java.net.URI;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class TachyonFileSystemTest {
    private static final LocalTachyonCluster cluster = new LocalTachyonCluster(256);
    private static final FileSystem fs = new TachyonFileSystem();
    private static String SCHEME_HOST;
    private static Path testDir1;
    private static Path testDir2;
    private static Path testFilePath1;
    private static Path testFilePath2;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SCHEME_HOST = "tachyon://" + InetAddress.getLocalHost().getCanonicalHostName() + ":18998";
        testDir1 = new Path(SCHEME_HOST + "/test");
        testDir2 = new Path(SCHEME_HOST + "/test/result");
        testFilePath1 = new Path(SCHEME_HOST + "/test/file1");
        testFilePath2 = new Path(SCHEME_HOST + "/test/file2");

        cluster.start();
        fs.initialize(URI.create(SCHEME_HOST));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        cluster.stop();
    }

    @After
    public void tearDown() throws IOException {
        if (fs.exists(testDir1)) {
            fs.delete(testDir1, true);
        }
    }

    @Test
    public void testWordCount() throws Exception {
        fs.mkdirs(testDir1);
        FSDataOutputStream out = fs.create(testFilePath1, true, 0, (short)0, 32);
        for (int x = 0; x < 10; x++) {
            out.write("hello\n".getBytes());
            out.write("world\n".getBytes());
        }
        out.close();
        WordCount.main(new String[]{"1", testFilePath1.toString(), testDir2.toString()});
        FSDataInputStream in = fs.open(testDir2);
        String text="";
        for(int x=0;x<18;x++){
            text+=(char)in.read();
        }
        assertEquals("hello 10\nworld 10\n",text);
    }

    @Test
    public void testGetFileStatus() throws Exception {
        fs.mkdirs(testDir1);
        fs.create(testFilePath1, true);
        FileStatus info = fs.getFileStatus(testFilePath1);
        assertEquals(0, info.getLen());
        assertEquals(testFilePath1, info.getPath());
        assertFalse(info.isDir());
    }

    @Test
    public void testGetFileBlockLocations() throws Exception {
        fs.mkdirs(testDir1);
        BlockLocation[] info;
        FSDataOutputStream out;

        out = fs.create(testFilePath1, true, 0, (short) 0, 8);
        out.write("i-fit".getBytes());
        out.close();

        info = fs.getFileBlockLocations(fs.getFileStatus(testFilePath1), 0, 1);
        assertEquals(1, info.length);

        out = fs.create(testFilePath2, true, 0, (short) 0, 8);
        out.write("i-am-too-big-for-one-block".getBytes());
        out.close();

        info = fs.getFileBlockLocations(fs.getFileStatus(testFilePath2), 0, 15);
        assertEquals(2, info.length);
    }

    @Test
    public void testOpenPath1() throws Exception {
        fs.mkdirs(testDir1);

        FSDataOutputStream out = fs.create(testFilePath1, true);
        out.write("teststring".getBytes());
        out.close();

        FSDataInputStream in = fs.open(testFilePath1, 4);
        assertTrue(in.read() == 116);
        in.close();
    }

    @Test
    public void testOpenPath2() throws Exception {
        fs.mkdirs(testDir1);

        FSDataOutputStream out = fs.create(testFilePath1, true);
        out.write("teststring".getBytes());
        out.close();

        FSDataInputStream in = fs.open(testFilePath1);
        assertTrue(in.read() == 116);
        in.close();
    }

    @Test
    public void testListStatus() throws Exception {
        fs.mkdirs(testDir1);
        fs.create(testFilePath1, true);
        fs.create(testFilePath2, true);
        FileStatus[] info = fs.listStatus(testDir1);
        assertEquals(2, info.length);
    }

    @Test
    public void testDelete() throws Exception {
        fs.mkdirs(testDir1);
        fs.create(testFilePath1, true);

        fs.delete(testFilePath1, true);
        assertFalse(fs.exists(testFilePath1));

        fs.delete(testDir1, true);
        assertFalse(fs.exists(testDir1));
    }

    @Test
    public void testMkdirs() throws Exception {
        fs.mkdirs(testDir1);
        assertTrue(fs.exists(testDir1));
    }

    @Test
    public void testCreate5() throws Exception {
        fs.mkdirs(testDir1);
        fs.create(testFilePath1, true, 0, (short) 0, 1048576);

        FileStatus info = fs.getFileStatus(testFilePath1);
        assertTrue(info.getBlockSize() == 1048576);
    }

    @Test
    public void testCreate2() throws Exception {
        fs.create(testDir1, true);

        assertTrue(fs.exists(testDir1));
    }

    @Test
    public void testRename() throws Exception {
        fs.mkdirs(testDir1);
        fs.create(testFilePath1, true);
        fs.rename(testFilePath1, testFilePath2);

        assertFalse(fs.exists(testFilePath1));
        assertTrue(fs.exists(testFilePath2));
    }
}
