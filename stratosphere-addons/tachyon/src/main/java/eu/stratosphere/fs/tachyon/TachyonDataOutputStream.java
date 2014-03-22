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
