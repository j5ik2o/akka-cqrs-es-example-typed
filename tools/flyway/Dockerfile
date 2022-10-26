FROM flyway/flyway

COPY conf /flyway/conf
COPY sql /flyway/sql
COPY drivers /flyway/drivers

ENV PATH="/flyway:${PATH}"

ENTRYPOINT ["flyway"]
CMD ["-?"]