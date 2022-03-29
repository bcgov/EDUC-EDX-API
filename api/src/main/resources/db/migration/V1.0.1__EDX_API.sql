CREATE TABLE EDX_SHEDLOCK
(
    NAME       VARCHAR(64),
    LOCK_UNTIL TIMESTAMP(3) NULL,
    LOCKED_AT  TIMESTAMP(3) NULL,
    LOCKED_BY  VARCHAR(255),
    CONSTRAINT EDX_PK PRIMARY KEY (NAME)
);
COMMENT ON TABLE EDX_SHEDLOCK IS 'This table is used to achieve distributed lock between pods, for schedulers.';