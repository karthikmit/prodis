# ProDis

ProDis - **In Process Dictionary Server** - is an Disk based LRU cache. It shall be configured to keep defined number of keys in memory.
Keys will be evicted out of memory based on LRU algorithm and evicted entries will be persisted in the configured local folder.

There are two maven modules in the project.

##ProDict

This is the core library which has very minimal dependencies and shall be used in any project as a JAR.
The core class ProDict exposes all the needed methods, for getting and putting key/value pairs.

##ProDisServer

This is an Netty based HTTP Server based on ProDict library. 
This server shall be run independently in private networks as a cache server.

##Things TO DO

1. Proper SnapShot(In Memory entries to FileSystem Sync) mechanism should be added up.
2. PersistenceManager should be thread safe.
3. Unit testing of ProDisServer.
4. Better persistence mechanism rather than storing individual buckets in separate files. Check [this SO link](http://stackoverflow.com/questions/35477294/storing-information-across-multiple-files-vs-single-file-with-offsets-in-index)