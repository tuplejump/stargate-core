Installation
=============

Stargate-core is currently tested on Cassandra 2.0.7,8,9. 
Stargate-search is still in the works and will be accessible soon.


Install from binaries
^^^^^^^^^^^^^^^^^^^^^
* Extract the archives downloaded from the download link. 
* Copy the jars from the lib folder of the extracted archive into your cassandra installation **lib** folder and you are good to go.


Install from source
^^^^^^^^^^^^^^^^^^^^

* Prerequisites - Java 1.7, Cassandra 2.0.9, Gradle. 

* Checkout the master branch in the git-repo.
	
* Run 'gradle jar' in the stargate-core directory. This will create the required libraries in build/libs.

* Drop the libraries into your cassandra installation **lib** folder and you are good to go.

Important Note on Shutdown procedure
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
.. warning ::
	For Stargate enabled Cassandra, shutdown Cassandra using *kill -15 cassandra-pid*. 

	Alternately, request a flush from nodetool and then shutdown Cassandra using kill -9.

	Cassandra can be shutdown with kill -9 *cassandra-pid* but, some writes to the index may not be flushed when using this method. 

Stargate flushes indexes periodically or when you request a flush, and also with a Shutdown hook. All the writes to the index are guaranteed to be flushed only when you explicitly call flush or shutdown using the kill -15 (since kill -9 does not called Shutdown hooks on the JVM). Otherwise, some writes to the index will be lost. 

Development usage
^^^^^^^^^^^^^^^^^^
For use in development, we will publish the stargate-core to Maven Central shortly.


