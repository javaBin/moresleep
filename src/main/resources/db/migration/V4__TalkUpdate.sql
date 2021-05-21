create table talkupdate(
    talkid text,
    conferenceid text,
    updatedby text,
    updatedat timestamp
);

insert into talkupdate(talkid, conferenceid, updatedby, updatedat)
select id,conferenceid,'UNKNOWN',lastupdated from talk;