#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    create user localdevuser with login password 'localdevuser';
    CREATE DATABASE moresleeplocal with owner localdevuser;
    CREATE DATABASE moresleepunit with owner localdevuser;
EOSQL