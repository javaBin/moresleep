FROM postgres:12.4
EXPOSE 5432
COPY postgressetup.sh /docker-entrypoint-initdb.d/init-user-db.sh
ENV POSTGRES_PASSWORD bingo
