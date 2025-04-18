alter table talkupdate add payload text null;

create table speakerupdate(
    id text,
    talkid text,
    conferenceid text,
    name text,
    email text,
    data text,
    updatedby text,
    updatedat timestamp
);

insert into speakerupdate(id,talkid,conferenceid,name,email,data,updatedby,updatedat)
select s.id,t.id,t.conferenceid,s.email,s.email,s. data,'UNKNOWN',t.lastupdated
from speaker s,talk t where s.talkid  = t.id;