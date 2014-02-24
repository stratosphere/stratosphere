package eu.stratosphere.fs.tachyon;

import eu.stratosphere.core.fs.FSDataOutputStream;
import java.io.IOException;
import tachyon.client.OutStream;

/**
 * Implementation of the {@link FSDataOutputStream} interface for the Tachyon File System.
 */
public class TachyonDataOutputStream extends FSDataOutputStream {
    private final OutStream outStream;

    public TachyonDataOutputStream(OutStream outStream) {
        this.outStream = outStream;
    }

    @Override
    public void write(int b) throws IOException {
        outStream.write(b);
    }

    @Override
    public void close() throws IOException {
        outStream.close();
    }
}
