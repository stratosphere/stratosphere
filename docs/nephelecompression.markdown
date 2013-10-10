---
layout: documentation
---
Using Nephele with Channel Compression
======================================

<table>
<thead>
<tr class="header">
<th align="left">Attention:</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td align="left">This is an experimental feature which is not yet officially supported by the Stratosphere team.</td>
</tr>
</tbody>
</table>

In order to reduce the amount of data that is transfered between the
individual tasks of a job, Nephele supports various compression
libraries. The data compression can be applied to Nephele's network and
file channels. Unfortunately, the licenses of the individual compression
libraries are not directly compatible with the Apache license the
Stratosphere project is under. As a result, the components cannot be
shipped with the official Stratosphere build, but must be compiled and
included separately.

The following documentation will describe the compilation process for
the individual compression libraries and highlight how to include and
use the compression features for your jobs. All libraries have different
trade-offs in compression time and compression rate, i.e., QuickLZ is
very fast but the best compression rates are achieved with bzip2 oder
LZMA.

Compiling the Compression Libaries
----------------------------------

The following subsection illustrates how to compile the individual
compression libraries and include them into your Nephele installation.

### nephele-compression-bzip2

The module **nephele-compression-bzip2** provides wrapper classes for
the [bzip2 compression
library](http://www.bzip.org "http://www.bzip.org"). The module is part
of the official Stratosphere source tree. To enable the module, please
take the following steps:

1. Download the bzip2 source tarball from
[here](http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz "http://www.bzip.org/1.0.6/bzip2-1.0.6.tar.gz")

2. Extract the downloaded archive to a temporary directory

    $ tar xvfz bzip2-1.0.6.tar.gz

3. Change into the subdirectory *bzip2-1.0.6* and copy the sources files
into the nephele-compression-bzip2 module's source directory

    $ cd bzip2-1.0.6
    $ cp -v *.c *.h ~/stratosphere/nephele/nephele-compression-bzip2/src/main/native/bzip2/bzip2

4. Run *mvn* to compile and package the module

    $ cd ~/stratosphere/nephele/nephele-compression-bzip2/
    $ mvn clean package

5. Copy the created jar file into the Nephele *lib* directory. Please
note that the created jar file contains native code and is therefore
platform dependent!

    $ cp target/nephele-compression-bzip2-0.1.jar <path_to_your_nephele_directory>/lib

To assign this module to any of Nephele's different compression levels
use the following class name:

*eu.stratosphere.nephele.io.compression.library.bzip2.Bzip2Library*

### nephele-compression-lzma

The module **nephele-compression-lzma** provides wrapper classes for the
[LZMA compression
library](http://7-zip.org/sdk.html "http://7-zip.org/sdk.html"). The
module is part of the official Stratosphere source tree. To enable the
module, please take the following steps:

1. Download the LZMA SDK from
[here](http://downloads.sourceforge.net/sevenzip/lzma920.tar.bz2 "http://downloads.sourceforge.net/sevenzip/lzma920.tar.bz2")

2. Extract the downloaded archive to a temporary directory

    $ tar xvfj lzma920.tar.bz2

3. Change into the subdirectory *C* and copy the files into the
nephele-compression-lzma module's source directory

    $ cd C
    $ cp -Rv * ~/stratosphere/nephele/nephele-compression-lzma/src/main/native/lzma/lzma/

4. Run *mvn* to compile and package the module

    $ cd ~/stratosphere/nephele/nephele-compression-lzma/
    $ mvn clean package

5. Copy the created jar file into the Nephele *lib* directory. Please
note that the created jar file contains native code and is therefore
platform dependent!

    $ cp target/nephele-compression-lzma-0.1.jar <path_to_your_nephele_directory>/lib

To assign this module to any of Nephele's different compression levels
use the following class name:

*eu.stratosphere.nephele.io.compression.library.lzma.LzmaLibrary*

### nephele-compression-lzo

The module **nephele-compression-lzo** provides wrapper classes for the
[LZO compression
library](http://www.oberhumer.com/opensource/lzo/ "http://www.oberhumer.com/opensource/lzo/").
This module is under the GPL license. Therefore it is not part of the
official Stratosphere source tree. It can be retrieved via the
Stratosphere GPL repository. To do so, take the following steps:

1. Checkout the Stratosphere GPL repository and change into the
nephele-compression-lzo subdirectory

    $ git clone https:%%//%%www.stratosphere.eu/public/stratosphere-gpl.git
    $ cd stratosphere-gpl/nephele-compression-lzo

2. Run *mvn* to compile and package the module

    $ mvn clean package

3. Copy the created jar file into the Nephele *lib* directory. Please
note that the created jar file contains native code and is therefore
platform dependent!

    $ cp target/nephele-compression-lzo-0.1.jar <path_to_your_nephele_directory>/lib

To assign this module to any of Nephele's different compression levels
use the following class name:

*eu.stratosphere.nephele.io.compression.library.lzo.LzoLibrary*

### nephele-compression-quicklz

The module **nephele-compression-quicklz** provides wrapper classes for
the [QuickLZ compression
library](http://www.quicklz.com "http://www.quicklz.com"). This module
is under the GPL license. Therefore it is not part of the official
Stratosphere source tree. It can be retrieved via the Stratosphere GPL
repository. To do so, take the following steps:

1. Checkout the Stratosphere GPL repository and change into the
nephele-compression-quicklz subdirectory

    $ git clone https:%%//%%www.stratosphere.eu/public/stratosphere-gpl.git
    $ cd stratosphere-gpl/nephele-compression-quicklz

2. Run *mvn* to compile and package the module

    $ mvn clean package

3. Copy the created jar file into the Nephele *lib* directory. Please
note that the created jar file contains native code and is therefore
platform dependent!

    $ cp target/nephele-compression-quicklz-0.1.jar <path_to_your_nephele_directory>/lib

To assign this module to any of Nephele's different compression levels
use the following class name:

*eu.stratosphere.nephele.io.compression.library.quicklz.QuicklzLibrary*

### nephele-compression-zlib

The module **nephele-compression-zlib** provides wrapper classes for the
[zlib compression library](http://www.zlib.net "http://www.zlib.net").
The module is part of the official Stratosphere source tree. To enable
the module, please take the following steps:

1. Download the zlib source tarball from
[here](http://zlib.net/zlib-1.2.7.tar.gz "http://zlib.net/zlib-1.2.7.tar.gz")

2. Extract the downloaded archive to a temporary directory

    $ tar xvfz zlib-1.2.7.tar.gz

3. Change into the subdirectory *zlib-1.2.5* and copy the sources files
into the nephele-compression-zlib module's source directory

    $ cd zlib-1.2.7
    $ cp -Rv * ~/stratosphere/nephele/nephele-compression-zlib/src/main/native/zlib/zlib

4. Run *mvn* to compile and package the module

    $ cd ~/stratosphere/nephele/nephele-compression-zlib/
    $ mvn clean package

5. Copy the created jar file into the Nephele *lib* directory. Please
note that the created jar file contains native code and is therefore
platform dependent!

    $ cp target/nephele-compression-zlib-0.1.jar <path_to_your_nephele_directory>/lib

To assign this module to any of Nephele's different compression levels
use the following class name:

*eu.stratosphere.nephele.io.compression.library.zlib.ZlibLibrary*

Mapping Libraries to Compression Levels
---------------------------------------

Once you have built your desired compression modules, you must map them
to the different compression levels (*LIGHT*, *MEDIUM*, and *HEAVY*)
Nephele offers. The mapping is defined in the configuration file
*conf/nephele-user.xml*. By default, it includes the following mapping:
