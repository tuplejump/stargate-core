Quick start
================

Pre-requisites
^^^^^^^^^^^^^^^
Install Stargate as instructed in the installation.

Open cassandra/bin/cqlsh and optionally create a keyspace::

	CREATE KEYSPACE my_keyspace WITH replication = 
	{
	 'class': 'SimpleStrategy', 
	 'replication_factor' : 1 
	};

Change into your keyspace::

	USE MY_KEYSPACE;

Let us create a table named PERSON like so::

	CREATE TABLE PERSON (
	    id int primary key,
	    isActive boolean,
	    age int,
	    eyeColor varchar,
	    name text,
	    gender varchar,
	    company varchar,
	    email varchar,
	    phone varchar,
	    address text,
	    stargate text
	);


Creating a Row Index
^^^^^^^^^^^^^^^^^^^^^
A row index with name 'person_idx' can be created on a table named 'PERSON' like so::
	
	CREATE CUSTOM INDEX person_idx ON PERSON(stargate) USING
	'com.tuplejump.stargate.RowIndex' WITH options =
	{
	        'sg_options':'{
	                "fields":{
	                        "age":{},
	                        "eyeColor":{},
	                        "name":{},
	                        "gender":{},
	                        "company":{},
	                        "phone":{},
	                        "address":{}
	                        }
	        }'
	};

.. note::

	* You create an index on a meta column of CQL type text. The column name can be anything.
	* The meta column should be left empty. While inserting data, it is left out in the values list.
	* The meta column is used to return any meta information about the search such as score(relevance), positions for highlighting etc.
	* You specify options as - WITH options={'sg_options':'<json>'}. 
	* The sg_options string has to be a valid JSON. Note the Single quotes and Double quotes. 
	* You always need to create the RowIndex only on a column with a string CQL datatype i.e CQL type varchar,ascii or text.
	* The columns which need to be indexed are specified in the 'fields' object. 

The above statement will create a row index on the table person and will index the columns specified with appropriate data type mapping derived from the Cassandra data type. More details about this can be found in the Index options section.

Now go ahead and insert data like so::

	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(1,false,29,'green','Davidson Hurst','male','TALKOLA','davidsonhurst@talkola.com','+1 (950) 405-2257','691 Hampton Place, Felt, North Carolina, 8466');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(2,false,27,'black','Maxwell Kemp','male','AMTAP','maxwellkemp@amtap.com','+1 (800) 495-3822','466 Kenilworth Place, Fivepointville, Maryland, 6240');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(3,false,25,'black','Cecelia Cain','female','MAINELAND','ceceliacain@maineland.com','+1 (874) 590-2058','644 Broome Street, Rutherford, Delaware, 6271');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(4,true,28,'green','Morse Sanders','male','APEX','morsesanders@apex.com','+1 (857) 427-3391','786 Division Avenue, Rose, Rhode Island, 4217');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(5,true,25,'black','Fernandez Morse','male','OPTICALL','fernandezmorse@opticall.com','+1 (911) 442-2649','116 Suydam Place, Libertytown, Michigan, 2257');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(6,false,27,'brown','Ryan Ross','male','ZAPHIRE','ryanross@zaphire.com','+1 (843) 423-2420','804 Erskine Loop, Robinette, Marshall Islands, 9161');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(7,true,34,'brown','Avis Mosley','female','TETRATREX','avismosley@tetratrex.com','+1 (883) 461-3832','391 Heyward Street, Hayes, Alabama, 5934');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(8,false,29,'black','Juana Ewing','female','REPETWIRE','juanaewing@repetwire.com','+1 (809) 410-2791','510 Lake Avenue, Austinburg, Virgin Islands, 2964');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(9,false,33,'brown','Edwards Patton','male','MANGELICA','edwardspatton@mangelica.com','+1 (977) 508-2935','131 Stone Avenue, Cucumber, Minnesota, 4601');
	INSERT INTO PERSON(id,isActive,age,eyeColor,name,gender,company,email,phone,address) VALUES(10,false,38,'blue','Weaver Carson','male','ISOLOGIX','weavercarson@isologix.com','+1 (916) 566-2681','560 Hanson Place, Gardners, Puerto Rico, 7821');

Once you have done that, you are now ready to query.

Querying a Row Index
^^^^^^^^^^^^^^^^^^^^^
Here is a list of quick queries that can be made using the default options. For more information on queries, read the Queries section ::

	-- select all people with age more than 30
	SELECT * FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "range",
	        field: "age",
	        lower: 30
	    }
	}';

	-- select all person with age more than 30 and less than 35
	SELECT name,age,email FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "range",
	        field: "age",
	        lower: 30,
	        upper:35
	    }
	}';

	-- get the person called Avis
	SELECT * FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "match",
	        field: "name",
	        value: "Avis"
	    }
	}';

	-- find people living in some street.
	SELECT * FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "match",
	        field: "address",
	        value: "street"
	    }
	}';

	-- find people starting with m.
	SELECT * FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "wildcard",
	        field: "name",
	        value: "m*"
	    }
	}';


	-- find companies starting with a.
	SELECT * FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "prefix",
	        field: "company",
	        value: "a"
	    }
	}';

	-- find companies from 'a'to 'p' and sort by name reverse
	SELECT name,company FROM PERSON WHERE stargate ='{
	    filter: {
	        type: "range",
	        field: "company",
	        lower: "a",
	        upper: "p"
	    },
	    sort:{
	       fields: [{field:"name",reverse:true}]
	    }
	}';





