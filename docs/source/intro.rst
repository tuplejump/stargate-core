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

