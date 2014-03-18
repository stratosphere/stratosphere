package eu.stratosphere.fs.tachyon;

import eu.stratosphere.core.fs.BlockLocation;
import eu.stratosphere.core.fs.FSDataInputStream;
import eu.stratosphere.core.fs.FSDataOutputStream;
import eu.stratosphere.core.fs.FileStatus;
import eu.stratosphere.core.fs.FileSystem;
import eu.stratosphere.core.fs.Path;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import tachyon.client.ReadType;
import tachyon.client.TachyonFS;
import tachyon.client.WriteType;
import tachyon.thrift.ClientBlockInfo;
import tachyon.thrift.ClientFileInfo;
import tachyon.thrift.FileDoesNotExistException;
import tachyon.thrift.NetAddress;

/**
 * Implementation of the {@link FileSystem} interface for the Tachyon File System.
 * Path format: tachyon://host:port[/file[/file]]
 * URI format: tachyon://host:port
 */
public class TachyonFileSystem extends FileSystem {
    private TachyonFS fileSystem;
    private final Path workingDirectory;
    private URI name;

    public TachyonFileSystem() {
        this.workingDirectory = new Path("/");
    }

    /**
     * Returns the current working directory. 
     * This directory is always the root directory.
     * @return working directory
     */
    @Override
    public Path getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Returns this filesystems URI.
     * @return uri
     */
    @Override
    public URI getUri() {
        return name;
    }

    /**
     * Initializes this file system.
     * @param name uri containing scheme and authority
     * Tachyon Uri format: tachyon://host:port
     * @throws IOException 
     */
    @Override
    public void initialize(URI name) throws IOException {
        this.name = URI.create(name.getScheme() + "://" + name.getAuthority());
        fileSystem = TachyonFS.get(name.toString());
    }

    /**
     * Returns a FileStatus for a file.
     * @param path file
     * @return FileStatus for given file
     * @throws IOException 
     */
    @Override
    public FileStatus getFileStatus(Path path) throws IOException {
        try {
            List<ClientFileInfo> tachyInfo = fileSystem.listStatus(pathToString(path.getParent()));
            int targetID = fileSystem.getFileId(pathToString(path));
            for (int x = 0; x < tachyInfo.size(); x++) {
                if (tachyInfo.get(x).id == targetID) {
                    return new TachyonFileStatus(tachyInfo.get(x), name);
                }
            }
            return null;
        } catch (IOException ioe) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public BlockLocation[] getFileBlockLocations(FileStatus file, long start, long len) throws IOException {
        int fileID = fileSystem.getFileId(pathToString(file.getPath()));
        List<ClientBlockInfo> blocks = fileSystem.getFileBlocks(fileID);

        String[] hosts;
        ArrayList<TachyonBlockLocation> temporaryLocations = new ArrayList();

        for (int x = 0; x < blocks.size(); x++) {
            ClientBlockInfo focusedBlock = blocks.get(x);
            if (focusedBlock.getOffset() + file.getBlockSize() >= start && focusedBlock.getOffset() <= start + len) {
                List<NetAddress> blockAddresses = blocks.get(x).getLocations();
                hosts = new String[blockAddresses.size()];
                for (int y = 0; y < blockAddresses.size(); y++) {
                    hosts[y] = blockAddresses.get(y).toString();
                }
                temporaryLocations.add(
                        new TachyonBlockLocation(hosts, blocks.get(x).getOffset(), blocks.get(x).getLength()));
            }
        }
        BlockLocation[] x = new BlockLocation[temporaryLocations.size()];
        for (int z = 0; z < temporaryLocations.size(); z++) {
            x[z] = temporaryLocations.get(z);
        }
        return x;
    }

    /**
     * Opens a file.
     * @param path file to open
     * @param bufferSize ignored
     * @return FSDataInputStream for the opened file
     * @throws IOException 
     */
    @Override
    public FSDataInputStream open(Path path, int bufferSize) throws IOException {
        return open(path);
    }

    /**
     * Opens a file.
     * @param path file to open
     * @return FSDataInputStream for the opened file
     * @throws IOException 
     */
    @Override
    public FSDataInputStream open(Path path) throws IOException {
        return new TachyonDataInputStream(fileSystem.getFile(pathToString(path)).getInStream(ReadType.CACHE));
    }

    /**
     * Provides a FileStatus[] for a given directory.
     * @param path directory
     * @return FileStatus[] containing information
     * @throws IOException 
     */
    @Override
    public FileStatus[] listStatus(Path path) throws IOException {
        List<ClientFileInfo> tachyInfo = fileSystem.listStatus(pathToString(path));
        FileStatus[] stratInfo = new FileStatus[tachyInfo.size()];
        for (int x = 0; x < tachyInfo.size(); x++) {
            stratInfo[x] = new TachyonFileStatus(tachyInfo.get(x), name);
        }
        return stratInfo;
    }

    /**
     * Deletes a file.
     * @param path file to delete
     * @param recursive boolean value indicating recursive deletion
     * @return
     * @throws IOException 
     */
    @Override
    public boolean delete(Path path, boolean recursive) throws IOException {
        return fileSystem.delete(pathToString(path), recursive);
    }

    /**
     * Creates a directory.
     * @param path directory to create
     * @return boolean value indicating success
     * @throws IOException 
     */
    @Override
    public boolean mkdirs(Path path) throws IOException {
        boolean result = fileSystem.mkdir(pathToString(path));
        return result;
    }

    /**
     * Creates a file.
     * @param path file to create
     * @param overwrite boolean value, indicating whether an existing file with the same name should be overwritten.
     * @param bufferSize ignored
     * @param replication ignored
     * @param blockSize desired blocksize
     * @return FSDataOutputStream for the created file
     * @throws IOException 
     */
    @Override
    public FSDataOutputStream create(Path path, boolean overwrite, int bufferSize, short replication, long blockSize)
            throws IOException {
        String filePath = pathToString(path);
        int fileID;
        if (fileSystem.exist(filePath)) {
            if (overwrite) {
                fileSystem.delete(filePath, true);
                fileID = fileSystem.createFile(filePath, blockSize);
            } else {
                fileID = fileSystem.getFileId(filePath);
            }
        } else {
            fileID = fileSystem.createFile(filePath, blockSize);
        }
        return new TachyonDataOutputStream(fileSystem.getFile(fileID).getOutStream(WriteType.CACHE_THROUGH));
    }

    /**
     * Creates a file.
     * @param path file to create
     * @param overwrite boolean value, indicating whether an existing file with the same name should be overwritten.
     * @return FSDataOutputStream for the created file
     * @throws IOException 
     */
    @Override
    public FSDataOutputStream create(Path path, boolean overwrite) throws IOException {
        String filePath = pathToString(path);
        int fileID;
        if (fileSystem.exist(filePath)) {
            if (overwrite) {
                fileSystem.delete(filePath, true);
                fileID = fileSystem.createFile(filePath);
            } else {
                fileID = fileSystem.getFileId(filePath);
            }
        } else {
            fileID = fileSystem.createFile(filePath);
        }
        return new TachyonDataOutputStream(fileSystem.getFile(fileID).getOutStream(WriteType.CACHE_THROUGH));
    }

    /**
     * Renames a file.
     * @param src file to rename
     * @param dst desired name
     * @return boolean value indicating success
     * @throws IOException 
     */
    @Override
    public boolean rename(Path src, Path dst) throws IOException {
        return fileSystem.rename(pathToString(src), pathToString(dst));
    }

    @Override
    public boolean isDistributedFS() {
        return true;
    }

    /**
     * Converts a path to a string, cutting of scheme and authority.
     * @param path path to convert
     * @return converted string
     */
    private String pathToString(Path path) {
        return path.toUri().getPath();
    }
}
