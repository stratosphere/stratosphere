package eu.stratosphere.fs.tachyon;

import eu.stratosphere.core.fs.BlockLocation;
import java.io.IOException;

/**
 * Implementation of the {@link BlockLocation} interface for the Tachyon File System.
 */
public class TachyonBlockLocation implements BlockLocation {
    private final String[] hosts;
    private final long offset;
    private final long length;

    public TachyonBlockLocation(String[] hosts, long offset, long length) {
        this.hosts = hosts;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public String[] getHosts() throws IOException {
        return hosts;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getLength() {
        return length;
    }

    /**
     * Compares two BlockLocations based on their offset.
     */
    @Override
    public int compareTo(BlockLocation o) {
        return (new Long(offset)).compareTo(o.getOffset());
    }
}
