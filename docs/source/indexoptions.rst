Index Configuration
======================

Index Creation Options
^^^^^^^^^^^^^^^^^^^^^^

An index is created with field mapping with the following syntax::

	CREATE CUSTOM INDEX (IF NOT EXISTS)? <index_name> 
                                  ON <table_name> ( <meta_column> ) 
                               USING 'com.tuplejump.stargate.RowIndex' 
                        WITH OPTIONS = 
                        {
                        	'sg_options':'<sg_options>'
                        }

where

	* <index_name> specifies the name of the index.
	* <table_name> is the name of the table on which you want to create a row index on.
	* <sg_options> needs to be a valid JSON as specified below.


The property 'sg_options' is a valid JSON object indicating the properties used to index columns in a CQL table. The root of this JSON object has various properties which indicate the default properties to be used in absence of per field properties. The root also has another JSON object field called 'fields' which indicates the names of columns to index and their corresponding properties.

Here are the various properties ::
	
	<sg_options> := 
	{
		type					: <datatype>,
		analyzer				: <analyzer>,
		tokenized				: <tokenized>,
		omitNorms				: <omitNorms>,
		striped					: <striping>,
		indexOptions				: <indexOptions>,
		numericPrecisionStep			: <numericPrecisionStep>,
		fields					: <sg_options>
	}

Fields
^^^^^^
The fields property is used to specify properties for data types with nesting i.e, object (used to index JSON) and map (used to index CQL maps).

Datatypes
^^^^^^^^^^
<datatype> indicates the data type of the indexed column. These are derived from the Cassandra column CQL type but may be overridden if specified explicitly. Datatypes give default behaviours for other properties of a field. The following are the available data types.

=====================	===================================================	==========================================================
Data type 				Behavior 											CQL Type
=====================	===================================================	==========================================================
    object				JSON type. Behaviour per Field 						JSON in CQL type text. Field has nested fields
    map 				Key and value behaviours							CQL map type
    text 				Standard analyzer, Tokenzied						CQL ascii/text type
    string 				Keyword analyzer, not tokenized						CQL varchar type
    integer				Keyword analyzer, not tokenized						CQL int type
    bigint 				Keyword analyzer, not tokenized						CQL bigint type	
    decimal				Keyword analyzer, not tokenized						CQL float type
    bigdecimal 			Keyword analyzer, not tokenized						CQL double/decimal type
    date 				Keyword analyzer, not tokenized						Field should be parsed as Date
    bool 				Keyword analyzer, not tokenized						CQL bool type

---------------------	---------------------------------------------------	----------------------------------------------------------
=====================	===================================================	==========================================================

JSON indexing
^^^^^^^^^^^^^
A data type of 'object' indicates that the CQL column(of type text) will contain a JSON. Each field in JSON will be indexed and queried separately. Nested field properties may be specified using 'parentname.childname' notation. For more details on using this, refer to the JSON indexing and querying section.

CQL collections
^^^^^^^^^^^^^^^
A CQL set and list data types by default, use the same type as that derived from the type of the element of the collection. Specifying properties for sets and list is therefore done in the same way as regular fields.

Map types have 2 or 3 indexed fields per entry depending on the type of the map key. The key and value types by default are derived according to the key and value CQL types. For map types with a non-tokenized type (from above table), an additional field with the key string value as 'name' and the value as 'value' is added to the lucene document. The properties of the key are set using 'colname.key' notation. Similarly for value 'colname.value' notation is used.

Tokenization
^^^^^^^^^^^^^
**<tokenized> default: as described above in the table.**

This splits your text into chunks and since different analyzers may use different tokenizers, you can get different output token streams, i.e. sequences of chunks of text. For example, KeywordAnalyzer doesn't split the text at all and takes all the field as a single token. At the same time, StandardAnalyzer (and most other analyzers) use spaces and punctuation as  split points. For example, for the phrase "I am very happy", it will produce a list ("i", "am", "very", "happy") or something like that. The text type by default inherits tokenized behaviour. This behaviour can be overridden by using the <tokenized> property.

Analyzers
^^^^^^^^^^
**<analyzer> default: as described above in the table.**

An Analyzer builds TokenStreams, which analyzes text. It thus represents a policy for extracting index terms from text. For more information on lucene Analyzers, read the lucene docs.

Out of box Analyzers with Stargate
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The following are the out of box lucene analyzers provided with Stargate. They can be used by specifying the <analyzer> property.
Read lucene docs for explanation on each of them.

	* StandardAnalyzer
	* WhitespaceAnalyzer
	* SimpleAnalyzer
	* KeywordAnalyzer 

Custom Analyzers
^^^^^^^^^^^^^^^^
Custom analyzers may be specified using the fully qualified class name. Lucene 5.5 custom analyzers are required.

Norms 
^^^^^^
**<omitNorms> default:true**

Norms allow index time boosts and field length normalization. This allows you to add boosts to fields at index time and makes shorter documents score higher. This may not be useful for short or non-full-text fields. Norms are stored in the index as a byte value per document per field. When norms are loaded up into an IndexReader, they are loaded into a byte[maxdoc] array for each field – so, even if one document out of 400 million has a field, it is still going to load byte[maxdoc] for that field, potentially using a lot of RAM. Considering turning norms off for certain fields, especially if you have a large number of fields in the index. Any field that is very short (i.e. not really a full text field – ids, names, keywords, etc), is a great candidate. For a large index, you might have to make some hard decisions and turn off norms for key full text fields as well. As an example of how much RAM we are talking about, one field in a 10 million doc index will take up just under 10 MB of RAM. One hundred such fields will take nearly a gigabyte of RAM. You can omit norms using the <omitNorms> property.

Index Options
^^^^^^^^^^^^^
**<indexOptions> default:DOCS**

This controls how much information is stored in the postings lists of the lucene index. For a detailed explanation, refer to lucene documentation. The available options are -

============================================    ===========================================================================
Option                                              Description
============================================    ===========================================================================
DOCS_AND_FREQS                                      Only documents and term frequencies are indexed: positions are omitted
DOCS_AND_FREQS_AND_POSITIONS                        Indexes documents, frequencies and positions.
DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS            Indexes documents, frequencies, positions and offsets.
DOCS                                                Only documents are indexed: term frequencies and positions are omitted.

--------------------------------------------    ---------------------------------------------------------------------------
============================================    ===========================================================================

Numeric field precision
^^^^^^^^^^^^^^^^^^^^^^^^
**<numericPrecisionStep> default:4**

Read lucene docs for explanation.

Striping/Sorting
^^^^^^^^^^^^^^^^
**<striped> default:none**

**Other options:also,only**

This controls whether the index value is stored in a striped/columnar fashion using Lucene doc values. Sortable fields need to be stored in this fashion. For any field which requires sorting use "also" (indicating a doc value field is stored in Lucene along with indexing the field) or "only"(indicating that only a doc value field is stored in lucene) as the option.

 











