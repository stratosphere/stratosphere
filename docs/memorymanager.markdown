---
layout: documentation
---
Memory Manager
--------------

Paged Memory Abstraction
------------------------

The MemoryManager interface is defined as part of the `nephele-common`
project in the package
*[eu.stratosphere.nephele.services.memorymanager](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/ "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/")*.
Memory is allocated in
*[MemorySegments](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/MemorySegment.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/MemorySegment.java")*,
which have a certain fixed size and are also referred to as memory
pages. The segments themselves can randomly access data at certain
positions as certain types (int, long, byte[], …).

All memory that is allocated in a task is bound to that specific task.
Once the task ends, no matter whether successful or not, the TaskManager
releases all memory the task had allocated, clearing any internal
references of the memory segments, such that any subsequent access to
the memory results in a NullPointerException.

Memory Views
------------

More typically than directly accessing the memory pages, the data is
accessed though views:
[DataInputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/DataInputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/DataInputView.java")
and
[DataOutputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/DataOutputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/DataOutputView.java").
The provide the abstraction of writing (or reading) sequentially to
(from) memory. Underneath, the data is distributed across the memory
pages.

Most memory views inherit from
[AbstractPagedInputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/AbstractPagedInputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/AbstractPagedInputView.java")
and
[AbstractPagedOutputView](https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/AbstractPagedOutputView.java "https://github.com/stratosphere-eu/stratosphere/tree/master/nephele/nephele-common/src/main/java/eu/stratosphere/nephele/services/memorymanager/AbstractPagedOutputView.java").
There are view implementations which transparently flush pages to disk
and retrieve them again when memory is scarce.

Default Memory Manager
----------------------

Nephele provides a
*[DefaultMemoryManager](https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/services/memorymanager/spi/DefaultMemoryManager.java "https://github.com/stratosphere/stratosphere/blob/master/nephele/nephele-server/src/main/java/eu/stratosphere/nephele/services/memorymanager/spi/DefaultMemoryManager.java")*.
It is started with a certain memory size. The size may be exactly
specified (in megabytes) in the Nephele configuration via the parameter
`taskmanager.memory.size` (see [Configuration
Reference](configreference.html "configreference")).
If the `taskmanager.memory.size` parameter is not specified, the
TaskManager instantiates the MemoryManager with a certain ratio of its
remaining memory. That amount is determined after all other Nephele
services have started and allocated their memory.

Internally, the memory manager reserves the memory by allocating byte
arrays upon startup time. At runtime it only distributed and re-collects
those byte arrays.
