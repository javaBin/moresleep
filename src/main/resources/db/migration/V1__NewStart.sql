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
)