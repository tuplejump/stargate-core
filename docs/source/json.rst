Indexing and querying JSON 
===========================

Pre-requisites
^^^^^^^^^^^^^^^
Install Stargate as instructed in the installation.

Open cassandra/bin/cqlsh and optionally create a keyspace::

	CREATE KEYSPACE my_keyspace WITH replication = 
	{
	 'class': 'SimpleStrategy', 
	 'replication_factor' : 1 
	};

Change into you keyspace::

	USE MY_KEYSPACE;

Let us create a table named PERSON_JSON like so::
	
	CREATE TABLE PERSON_JSON (
	    id int primary key,
	    json text,
	    stargate text
	);

Creating a index on JSON fields
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
The above table has just one column 'json' which is of CQL type text. We can push valid JSON into this column. To index this JSON we create an index on the 'stargate' meta column like so ::

	CREATE CUSTOM INDEX json_idx ON PERSON_JSON(stargate) USING
	'com.tuplejump.stargate.RowIndex' WITH options =
	{
	        'sg_options':'{
	                "fields":{
	                        "json":{
	                            "type":"object"
	                        }
	                }
	        }'
	};


.. note::
	
	* You create an index on a meta column of CQL type text. The column name can be anything.
	* The JSON column should be of CQL type text. 
	* In sg_options, you specify the type of the column as 'object'. This indicates that the object is a json.

Suppose you insert data into the table like so :: 

	INSERT INTO PERSON_JSON (id,json) values (1,'{
		"age": 40,
		"eyeColor": "green",
		"name": "Casey Stone",
		"gender": "female",
		"company": "EXODOC",
		"address": "760 Gold Street, Choctaw, Iowa, 3595",
		"registered": "2014-03-30T18:24:14 -06:-30",
		"latitude": 30.904815,
		"longitude": 169.113457,
		"tags": [
			"idiot",
			"fool",
			"bad"
		],
		"friends": [
			{
				"name": "Casey Stone"
			},
			{
				"name": "Clark Wise"
			},
			{
				"name": "Letitia Holder"
			}
		]
	}');
	INSERT INTO PERSON_JSON (id,json) values (2,'{
		"age": 20,
		"eyeColor": "brown",
		"name": "Selma Nelson",
		"gender": "female",
		"company": "WAAB",
		"address": "421 Dictum Court, Deltaville, Hawaii, 5115",
		"registered": "2014-05-13T23:42:48 -06:-30",
		"latitude": 88.721567,
		"longitude": -77.946054,
		"tags": [
			"good",
			"nice",
			"cool"
		],
		"friends": [
			{
				"name": "Casey Stone"
			},
			{
				"name": "Sweet Chambers"
			},
			{
				"name": "Cantor Wise"
			}
		]
	}');
	INSERT INTO PERSON_JSON (id,json) values (3,'{
		"age": 37,
		"eyeColor": "brown",
		"name": "Powers Brown",
		"gender": "male",
		"company": "EXODOC",
		"address": "527 Beard Street, Springhill, Iowa, 4189",
		"registered": "2014-05-15T01:38:29 -06:-30",
		"latitude": 11.414768,
		"longitude": -97.106062,
		"tags": [
			"bad",
			"ugly",
			"yuck"
		],
		"friends": [
			{
				"name": "Anthony Vaughan"
			},
			{
				"name": "Sweet Chambers"
			},
			{
				"name": "Cantor Hunt"
			}
		]
	}');
	INSERT INTO PERSON_JSON (id,json) values (4,'{
		"age": 34,
		"eyeColor": "blue",
		"name": "Mercer Roberts",
		"gender": "male",
		"company": "BEDDER",
		"address": "496 Thornton Street, Gwynn, Maine, 3535",
		"registered": "2014-02-21T09:08:57 -06:-30",
		"latitude": -59.376042,
		"longitude": 68.532665,
		"tags": [
			"friendly",
			"nice",
			"cool"
		],
		"friends": [
			{
				"name": "Casey Stone"
			},
			{
				"name": "Wooten Daugherty"
			},
			{
				"name": "Robyn Wynn"
			}
		]
	}');
	INSERT INTO PERSON_JSON (id,json) values (5,'{
		"age": 35,
		"eyeColor": "blue",
		"name": "Avila Quinn",
		"gender": "male",
		"company": "BEDDER",
		"address": "682 Beadel Street, Cawood, Arkansas, 9088",
		"registered": "2014-01-15T13:07:00 -06:-30",
		"latitude": -21.666006,
		"longitude": 137.589547,
		"tags": [
			"good",
			"bad",
			"ugly"
		],
		"friends": [
			{
				"name": "Patty Salas"
			},
			{
				"name": "Clark Wise"
			},
			{
				"name": "Casey Stone"
			}
		]
	}');

.. note::

	* In the above data all json fields become searchable as top level index fields. For example 'age' in the json becomes searchable 'age' in the index.
	* Nested fields become searchable top level fields with a 'parent.child' notation.
	* For example 'name' in 'friends' becomes searchable as 'friends.name'

Querying JSON
^^^^^^^^^^^^^^
With the index created and with the data inserted as above you can make basic queries such as these::
	
	-- find a person with name Avila
	SELECT * from PERSON_JSON where stargate= '{
	    query:{
	        type:"match",
	        field:"name",
	        value:"Avila"
	    }
	}';

	-- find people with a friend called Patty
	SELECT * from PERSON_JSON where stargate= '{
	    query:{
	        type:"match",
	        field:"friends.name",
	        value:"Patty"
	    }
	}';

	-- find people who have been tagged as good
	SELECT * from PERSON_JSON where stargate= '{
	    query:{
	        type:"match",
	        field:"tags",
	        value:"good"
	    }
	}';

However if you do the following query it would not work! ::

	-- find people with age 30
	-- this wont work until you change the mapping.
	SELECT * from PERSON_JSON where stargate= '{
	    query:{
	        type:"match",
	        field:"age",
	        value:35
	    }
	}';

This is because although Stargate indexes numeric fields as numeric, while querying it would not understand that it needs to query it numerically. So you change the mapping as follows ::
	
	DROP INDEX json_idx;

	CREATE CUSTOM INDEX json_idx ON PERSON_JSON(stargate) USING
	'com.tuplejump.stargate.RowIndex' WITH options =
	{
	        'sg_options':'{
	                "fields":{
	                        "json":{
	                            "type":"object",
	                            "fields":{
	                                "age":{ "type":"integer"}
	                            }
	                        }
	                }
	        }'
	};

This mapping tells Stargate that the field needs to be queried a an integer. Now the above query will work as expected.

For more details on configuration read the Index Options section.




