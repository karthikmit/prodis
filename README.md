# prodis
ProDis - In Process Disk Server - is an Disk based LRU cache. It shall be configured to keep defined number of keys in memory. 
Keys will be evicted out of memory based on LRU algorithm and evicted entries will be persisted in the configured local folder.

There are two maven modules in the project.

#ProDict

This is the core library which has very minimal dependencies and shall be used in any project as a JAR.
The core class ProDict exposes all the needed methods, for getting and putting key/value pairs.

#ProDisServer

This is an Netty based HTTP Server based on ProDict library. 
This server shall be run independently in private networks as a cache server.

