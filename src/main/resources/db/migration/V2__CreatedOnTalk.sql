alter table talk add created timestamp;

update talk set created = lastupdated;