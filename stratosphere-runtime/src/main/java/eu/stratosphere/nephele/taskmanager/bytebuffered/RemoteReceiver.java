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

package eu.stratosphere.nephele.taskmanager.bytebuffered;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import eu.stratosphere.core.io.IOReadableWritable;
import eu.stratosphere.util.StringUtils;

/**
 * Objects of this class uniquely identify a connection to a remote {@link eu.stratosphere.nephele.taskmanager.TaskManager}.
 * 
 */
public final class RemoteReceiver implements IOReadableWritable {

	/**
	 * The address of the connection to the remote {@link eu.stratosphere.nephele.taskmanager.TaskManager}.
	 */
	private InetSocketAddress connectionAddress;

	/**
	 * Constructs a new remote receiver object.
	 * 
	 * @param connectionAddress
	 *        the address of the connection to the remote {@link eu.stratosphere.nephele.taskmanager.TaskManager}
	 */
	public RemoteReceiver(final InetSocketAddress connectionAddress) {

		if (connectionAddress == null) {
			throw new IllegalArgumentException("Argument connectionAddress must not be null");
		}

		this.connectionAddress = connectionAddress;
	}

	/**
	 * Default constructor for serialization/deserialization.
	 */
	public RemoteReceiver() {
		this.connectionAddress = null;
	}

	/**
	 * Returns the address of the connection to the remote {@link eu.stratosphere.nephele.taskmanager.TaskManager}.
	 * 
	 * @return the address of the connection to the remote {@link eu.stratosphere.nephele.taskmanager.TaskManager}
	 */
	public InetSocketAddress getConnectionAddress() {

		return this.connectionAddress;
	}

	@Override
	public int hashCode() {

		return this.connectionAddress.hashCode();
	}


	@Override
	public boolean equals(final Object obj) {

		if (!(obj instanceof RemoteReceiver)) {
			return false;
		}

		final RemoteReceiver rr = (RemoteReceiver) obj;
		if (!this.connectionAddress.equals(rr.connectionAddress)) {
			return false;
		}

		return true;
	}


	@Override
	public void write(final DataOutput out) throws IOException {

		final InetAddress ia = this.connectionAddress.getAddress();
		out.writeInt(ia.getAddress().length);
		out.write(ia.getAddress());
		out.writeInt(this.connectionAddress.getPort());
	}


	@Override
	public void read(final DataInput in) throws IOException {

		final int addr_length = in.readInt();
		final byte[] address = new byte[addr_length];
		in.readFully(address);

		InetAddress ia = null;
		try {
			ia = InetAddress.getByAddress(address);
		} catch (UnknownHostException uhe) {
			throw new IOException(StringUtils.stringifyException(uhe));
		}
		final int port = in.readInt();
		this.connectionAddress = new InetSocketAddress(ia, port);
	}


	@Override
	public String toString() {

		return this.connectionAddress.toString();
	}
}
