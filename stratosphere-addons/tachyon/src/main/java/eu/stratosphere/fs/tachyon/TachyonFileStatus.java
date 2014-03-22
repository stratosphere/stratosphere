/***********************************************************************************************************************
 * Copyright (C) 2010-2013 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 **********************************************************************************************************************/

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
