create table conference(
    id text,
    slug text,
    name text
);

create table talk(
    id text,
    conferenceid text,
    data text,
    publicdata text,
    status text,
    postedby text,
    lastupdated timestamp,
    publishedat timestamp
);

create table speaker(
    id text,
    talkid text,
    conferenceid text,
    name text,
    email text,
    data text
);