CREATE KEYSPACE my_keyspace WITH replication =
{
 'class': 'SimpleStrategy',
 'replication_factor' : 1
};

USE MY_KEYSPACE;

CREATE TABLE PERSON (
    id int primary key,
    isActive boolean,
    age int,
    eyeColor varchar,
    name varchar,
    gender varchar,
    company varchar,
    email varchar,
    phone varchar,
    address text,
    stargate text
);

CREATE CUSTOM INDEX person_idx ON PERSON(stargate) USING
'com.tuplejump.stargate.RowIndex' WITH options =
{
        'sg_options':'{
                "fields":{
                        "age":{},
                        "eyeColor":{},
                        "name":{},
                        "gender":{},
                        "eyeColor":{},
                        "company":{},
                        "email":{},
                        "phone":{},
                        "address":{}
                }
        }'
};

CREATE TABLE PERSON_JSON (
    id int primary key,
    json text,
    stargate text
);

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