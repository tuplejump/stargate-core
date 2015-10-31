Installation
=============

Stargate-core is currently tested on Cassandra 2.1.10, 2.1.11


Install from binaries
^^^^^^^^^^^^^^^^^^^^^
* Extract the archives downloaded from the download link. 
* Copy the jars from the lib folder of the extracted archive into your cassandra installation **lib** folder and you are good to go.


Install from source
^^^^^^^^^^^^^^^^^^^^

* Prerequisites - Java 1.8, Cassandra 2.1.10/11, Gradle.

* Checkout the master branch in the git-repo.
	
* Run 'gradle jar' in the stargate-core directory. This will create the required libraries in build/libs.

* Drop the libraries into your cassandra installation **lib** folder and you are good to go.

Important Note on Shutdown procedure
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. warning ::
	For Stargate enabled Cassandra, shutdown Cassandra using *kill -15 cassandra-pid*. 

	Alternately, request a flush from nodetool and then shutdown Cassandra using kill -9.

	Cassandra can be shutdown with kill -9 *cassandra-pid* but, some writes to the index may not be flushed when using this method. 
	
	This is usually fine when the node fails by itself. When a node fails it is simpler to purge it and replace it back into the cluster.

Stargate flushes indexes periodically or when you request a flush, and also with a Shutdown hook. All the writes to the index are guaranteed to be flushed only when you explicitly call flush or shutdown using the kill -15 (since kill -9 does not call Shutdown hooks on the JVM). Otherwise, some writes to the index will be lost. 

Development usage
^^^^^^^^^^^^^^^^^^
Stargate-core is in Maven central


