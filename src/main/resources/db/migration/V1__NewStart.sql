create table conference(
    id text,
    slug text,
    name text
);

create table talk(
    id text,
    conferenceid text,
    data text,
    status text,
    postedby text,
    lastupdated timestamp
);

create table speaker(
    id text,
    talkid text,
    name text,
    email text,
    data text
);