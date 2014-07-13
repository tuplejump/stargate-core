Queries
=============

A query is given as follows ::
	
	SELECT <selection> from <table> where <meta-column> = '{<query-parts}'

A query has 3 parts. Query, filter and sort ::
	
	{
		query:	{<query-options},
		filter:	{<query-options>},
		sort:	{<sort-options>}
	}

A query and filter have the same options i.e <query-options>. A query takes part in score calculation. The scores are output in the meta column in the result set. A filter simply filters the rows passing the conditions. 
By default the rows of the resultset are ordered by the score relevance. If you want the result sorted differently, the sorting order may be specified in the '<sort-options>'
	
Query and filter options
^^^^^^^^^^^^^^^^^^^^^^^^

A query or filter is as follows::
	
	{
		type:	<type>,
		<property> : <property-value>		
	}


Types of queries
^^^^^^^^^^^^^^^^

Lucene
------
A query using the lucene standard query parser syntax. 

.. topic:: Datatypes supported
	
	* All

.. topic:: Properties

	* *type :lucene*
	* *field*: The default value on which this lucene query is being made
	* *value*: The lucene query using query parser syntax.

Match
------
A query to match a value.

.. topic:: Datatypes supported
	
	* All

.. topic:: Properties

	* *type :match*
	* *field*: The field name for the value has to match.
	* *value*: The value of the field to match.

Phrase
------
Various values forming a phrase with a slop.

.. topic:: Datatypes supported
	
	* text

.. topic:: Properties

	* *type :phrase*
	* *field*: The field name for the value has to match.
	* *value*: The list of values of the phrase.
	* *slop*: How many words can be skipped between thewords in the phrase.

Fuzzy
------
Fuzzy searches based on the Levenshtein Distance.

.. note :: 
	
	* For fuzzy queries the index needs to store term vectors with position.
	* Hence while creating the index, Index options need to be specified as DOCS_AND_FREQS_AND_POSITIONS

.. topic:: Datatypes supported
	
	* text
	* string

.. topic:: Properties

	* *type :fuzzy*
	* *field*: The field name for the value has to match.
	* *value*: The value of the field to match.
	* *maxEdits*: **(default = 2)** Value between 0 and 2 (the Levenshtein automaton maximum supported distance).
	* *prefixLength*: **(default = 0)** Integer representing the length of common non-fuzzy prefix.
	* *maxExpansions*: **(default = 50)**: an integer for the maximum number of terms to match.


Prefix
------
A query to find values with the passed prefix.

.. topic:: Datatypes supported
	
	* text
	* string

.. topic:: Properties

	* *type :prefix*
	* *field*: The field name for which the value has to be prefixed with.
	* *value*: The value of the field to have the passed prefix.

Range
------
A range of values to match.

.. topic:: Datatypes supported
	
	* All

.. topic:: Properties

	* *type :range*
	* *field*: The field name for which the range is being specified.
	* *lower*: lower bound of the range. Defaults to lower value of the data type.
	* *includeLower*: **(default = false)** if the left value is included in the results (>=)
	* *upper*: upper bound of the range. Defaults to upper value of the data type.
	* *includeUpper*: **(default = false)** if the right value is included in the results (<=).


Regex
------
A query which can match the passed regex.

.. topic:: Datatypes supported
	
	* text
	* string

.. topic:: Properties

	* *type :regex*
	* *field*: The field name for which the value has to match the regex.
	* *value*: The value of the regex.

Wildcards
---------
A query which can match the passed wildcard.

.. topic:: Datatypes supported
	
	* text
	* string

.. topic:: Properties

	* *type :wildcard*
	* *field*: The field name for which the value has to match the wildcard.
	* *value*: The value of the wildcard expression.

Combining conditions
^^^^^^^^^^^^^^^^^^^^
Conditions can be combined using the boolean query option. A Boolean query can further contain nested boolean queries.
A Boolean query can have a must,should and not conditions.

.. topic:: Datatypes supported
	
	* All

.. topic:: Properties

	* *type :match*
	* *must*: a list of conditions that must occur in the value. Each condition is a query.
	* *should*: a list of conditions that should occur. Each condition is a query.
	* *not*: a list of conditions that should not occur. Each condition is a query


As a reference the table below lists the queries that are possible and along with the properties that are available for each type of query

+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| Query type   | Properties                                                                                              | Description                                                                           |
+==============+=========================================================================================================+=======================================================================================+
| lucene       | field: The default value on which this lucene query is being made                                       | A query using the lucene standard query parser syntax. All datatypes supported.       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The lucene query using query parser syntax.                                                      |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| match        | field: The field name for the value has to match                                                        | A query to match a value exactly. All datatypes supported.                            |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The value of the field to match.                                                                 |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| phrase       | field: The field name for the value has to match                                                        | Various values forming a phrase with a slop. For text types only.                     |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | values: The list of values of the phrase                                                                |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | slop: How many words can be skipped between thewords in the phrase                                      |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| fuzzy        | field: The field name for the value has to match                                                        | Fuzzy searches based on the Levenshtein Distance. For text and string types only.     |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The value of the field to match.                                                                 | \* Also need to specify indexOptions during creation                                  |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | maxEdits: (default = 2):Value between 0 and 2 (the Levenshtein automaton maximum supported distance).   | \* Index options need to have DOCS\_AND\_FREQS\_AND\_POSITIONS                        |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | prefixLength (default = 0): integer representing the length of common non-fuzzy prefix.                 |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | maxExpansions (default = 50): an integer for the maximum number of terms to match.                      |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| prefix       | field: The field name for the value has to be prefixed with                                             | A query to find values with the passed prefix. For text and string types only.        |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The value of the field to have the passed prefix.                                                |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| range        | field: The field name for which the range is being specified.                                           | A range of values to match.All datatypes supported.                                   |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | lower: lower bound of the range. Defaults to lower value of the data type.                              |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | includeLower (default = false): if the left value is included in the results (>=)                       |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | upper: upper bound of the range. Defaults to upper value of the data type.                              |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | includeUpper (default = false): if the right value is included in the results (<=).                     |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| regex        | field: The field name for which the value has to match the regex                                        | A query which can match the passed regex. For text and string types only.             |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The regex value                                                                                  |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| wildcard     | field: The field name for which the value has to match the wildcard                                     | A query with wild card expressions. For text and string types only.                   |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | value: The value with wildcards                                                                         |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
| boolean      | must: a list of conditions that must occur in the value. Each condition is a query.                     | A query which joins sub queries using a boolean condition. All datatypes supported.   |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | should: a list of conditions that should occur. Each condition is a query.                              |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+
|              | not: a list of conditions that should not occur. Each condition is a query                              |                                                                                       |
+--------------+---------------------------------------------------------------------------------------------------------+---------------------------------------------------------------------------------------+



Sort
^^^^^

A sort is specified as follows ::

	{
       fields: [
       	{field:<name>,reverse:<reverse>},
       	{field:<name>,reverse:<reverse>}...
       ]
    }

where <name> is the name of the field on which the sort is to be applied and reverse is specified optionally as true to reflect the sort order on a field.
