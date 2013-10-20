---
layout: documentation
---
I/O Manager
-----------

The I/O manager is Nephele's component that should be used by user
programs to write data to disk and read data back. The purpose is to
create a single I/O gateway to avoid too many concurrent reading and
writing threads and to allow asynchronous I/O operations, including
pre-fetching and write-behind. The I/O manager has a single thread for
serving read requests and one for write requests. Internally, I/O
operations are always performed in units of blocks. The memory used by
the I/O manager is in the form of memory segments from the
MemoryManager.

The I/O manager and the readers and writers are contained in the package
*[eu.stratosphere.nephele.services.iomanager](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager")*.
The I/O manager provides only abstractions for reading / writing entire
memory pages. Abstractions for record streams are given though
[ChannelReaderInputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/ChannelReaderInputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/ChannelReaderInputView.java")
and
[ChannelWriterOutputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/ChannelWriterOutputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/ChannelWriterOutputView.java").

The files creates by the I/O manager are temporary files that live only
in the scope of one job. They are by default created in the system's
temp directory and are described by channel IDs, which encapsulate their
path. The path of the temp files is configured with the parameter
*taskmanager.tmp.dir*. Refer to the [Configuration
Reference](configreference.html "configreference")
for details.

Block Channels
--------------

Block channels are instantiated with a channel ID (an internal id for
the file path) and a synchronized queue that is a source or drain of
buffers. The
[BlockChannelWriter](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/BlockChannelWriter.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/iomanager/BlockChannelWriter.java")
for example accepts memory segments (blocks), writes them as soon as
possible, and returns the memory block that was written to the given
queue. If the same queue is used to draw the next required memory
segments from, that mechanism realizes block write-behind I/O simply and
efficiently. The key difference to the stream channels is that blocks
are not owned exclusively by a channel during its life time.
