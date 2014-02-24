package eu.stratosphere.fs.tachyon;

import eu.stratosphere.fs.tachyon.TachyonFileSystem;
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
    private static final FileSystem fs = new TachyonFileSystem();
    private static final Path testDir = new Path("tachyon://127.0.0.1:19998/test");
    private static final Path testFilePath1 = new Path("tachyon://127.0.0.1:19998/test/file1");
    private static final Path testFilePath2 = new Path("tachyon://127.0.0.1:19998/test/file2");

    @BeforeClass
    public static void setUpClass() throws IOException {
        fs.initialize(URI.create("tachyon://127.0.0.1:19998"));
    }

    @After
    public void tearDown() throws IOException {
        if (fs.exists(testDir)) {
            fs.delete(testDir, true);
        }
    }

    @Test
    public void testGetFileStatus() throws Exception {
        fs.mkdirs(testDir);
        fs.create(testFilePath1, true);
        FileStatus info = fs.getFileStatus(testFilePath1);
        assertEquals(0, info.getLen());
        assertEquals(testFilePath1, info.getPath());
        assertFalse(info.isDir());
    }

    @Test
    public void testGetFileBlockLocations() throws Exception {
        fs.mkdirs(testDir);
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
        fs.mkdirs(testDir);

        FSDataOutputStream out = fs.create(testFilePath1, true);
        out.write("teststring".getBytes());
        out.close();

        FSDataInputStream in = fs.open(testFilePath1, 4);
        assertTrue(in.read() == 116);
        in.close();
    }

    @Test
    public void testOpenPath2() throws Exception {
        fs.mkdirs(testDir);

        FSDataOutputStream out = fs.create(testFilePath1, true);
        out.write("teststring".getBytes());
        out.close();

        FSDataInputStream in = fs.open(testFilePath1);
        assertTrue(in.read() == 116);
        in.close();
    }

    @Test
    public void testListStatus() throws Exception {
        fs.mkdirs(testDir);
        fs.create(testFilePath1, true);
        fs.create(testFilePath2, true);
        FileStatus[] info = fs.listStatus(testDir);
        assertEquals(2, info.length);
    }

    @Test
    public void testDelete() throws Exception {
        fs.mkdirs(testDir);
        fs.create(testFilePath1, true);

        fs.delete(testFilePath1, true);
        assertFalse(fs.exists(testFilePath1));

        fs.delete(testDir, true);
        assertFalse(fs.exists(testDir));
    }

    @Test
    public void testMkdirs() throws Exception {
        fs.mkdirs(testDir);
        assertTrue(fs.exists(testDir));
    }

    @Test
    public void testCreate5() throws Exception {
        fs.mkdirs(testDir);
        fs.create(testFilePath1, true, 0, (short) 0, 1048576);

        FileStatus info = fs.getFileStatus(testFilePath1);
        assertTrue(info.getBlockSize() == 1048576);
    }

    @Test
    public void testCreate2() throws Exception {
        fs.create(testDir, true);

        assertTrue(fs.exists(testDir));
    }

    @Test
    public void testRename() throws Exception {
        fs.mkdirs(testDir);
        fs.create(testFilePath1, true);
        fs.rename(testFilePath1, testFilePath2);

        assertFalse(fs.exists(testFilePath1));
        assertTrue(fs.exists(testFilePath2));
    }
}
