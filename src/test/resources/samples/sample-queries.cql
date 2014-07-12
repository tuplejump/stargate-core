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

-- find companies from a to p and sort by name reverse
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

-- find people with age 30
-- this wont work until you change the mapping.
SELECT * from PERSON_JSON where stargate= '{
    query:{
        type:"match",
        field:"age",
        value:35
    }
}';