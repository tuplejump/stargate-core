What is Stargate
=================

Stargate is made of 2 components.

* Stargate-core - To add Lucene indexing support in Cassandra. (See installation)
* Stargate-search - A search server like Solr/ElasticSearch (Work in progress)

Stargate-core Features
^^^^^^^^^^^^^^^^^^^^^^
1. Add lucene based row indices to Cassandra CQL tables.
2. Index and query JSON data directly.
3. Index CQL maps,sets and lists.
4. Query, filter and sort based on fields in row index.
5. Specify different data types and analyzers for lucene analysis and querying.
6. Use a variety of lucene queries like match, range, phrase, wildcard, regex, fuzzy, prefix and more.


How does simplify my Dev/ops
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

1. If you are looking to add Row Indices and full-text search to Cassandra, Stargate-core does the job. You will not need to have another Solr/ES/DSE cluster for simple needs.

2. If you are looking at a simple search cloud using Solr, Stargate-search should be a compelling alternative. Cassandra's deployment model is almost zero-ops and multi-datacenter for true HA. This inherits all of that. You should take a look at Cassandra as a data store instead of the traditional databases which do not scale out. It is trivial to add and release capacity. You can keep the partition of data and the indices for the data all at one place. This will be replicated like in normal Cassandra.

3. If you are looking at/maintaining a separate Solr/Cloud installation together with Cassandra. Then this takes out the pain of maintaing separate deployments for Zookeeper(for availability of Solr), Solr nodes and separate Cassandra nodes. It also means a simpler development workflow where you just need to use CQL through a regular JDBC driver. All the great features of Cassandra that you know like elastic scale, multi datacenter, simple ops etc are inherited.

How does it compare to Lucandra/Solandra/Datastax Enterprise search(DSE)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Lucandra/Solandra isn't maintained anymore. DSE is the only integration available with Cassandra. We are inspired by Solandra/DSE and want to make a compelling, feature comparable alternative. 

That said - Stargate is different from Solandra and is a bit more like DSE. Solandra uses Cassandra column families to store the Solr/Lucene Index information. In contrast Stargate/DSE use the native Lucene index format to store the index locally per node. The raw field data is not stored in the Lucene index however(unless you tell it so and even then), that is still fetched from the column family. 

By doing this, Stargate/DSE can keep the data in sync per node and provide consistency and durability guarantees of Cassandra while inheriting all the performance effort that goes into Lucene. 

Also unlike Solandra/DSE, Stargate uses Lucene directly instead of Solr. The distributed search part is handled just like any Cassandra query.