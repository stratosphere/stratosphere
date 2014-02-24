package eu.stratosphere.fs.tachyon;

import eu.stratosphere.core.fs.FileStatus;
import eu.stratosphere.core.fs.Path;
import java.net.URI;
import tachyon.thrift.ClientFileInfo;

/**
 * Implementation of the {@link FileStatus} interface for the Tachyon File System.
 */
public class TachyonFileStatus implements FileStatus {
    private final long blockLength;
    private final long blockSize;
    private final boolean isDirectory;
    private final Path path;

    public TachyonFileStatus(ClientFileInfo info, URI name) {
        blockLength = info.length;
        blockSize = info.blockSizeByte;
        isDirectory = info.isFolder();
        path = new Path(name + info.getPath());
    }

    @Override
    public long getLen() {
        return blockLength;
    }

    @Override
    public long getBlockSize() {
        return blockSize;
    }

    @Override
    public short getReplication() {
        return 1;
    }

    @Override
    public long getModificationTime() {
        return 0; //information not available
    }

    @Override
    public long getAccessTime() {
        return 0; //information not available
    }

    @Override
    public boolean isDir() {
        return isDirectory;
    }

    @Override
    public Path getPath() {
        return path;
    }
}
