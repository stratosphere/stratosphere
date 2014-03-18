package eu.stratosphere.fs.tachyon;

import eu.stratosphere.core.fs.FSDataInputStream;
import java.io.IOException;
import tachyon.client.InStream;

/**
 * Implementation of the {@link FSDataInputStream} interface for the Tachyon File System.
 */
public class TachyonDataInputStream extends FSDataInputStream {
    private final InStream inStream;

    public TachyonDataInputStream(InStream inStream) {
        this.inStream = inStream;
    }

    @Override
    public void seek(long desired) throws IOException {
        inStream.seek(desired);
    }

    @Override
    public int read() throws IOException {
        return inStream.read();
    }

    @Override
    public void close() throws IOException {
        inStream.close();
    }
}
