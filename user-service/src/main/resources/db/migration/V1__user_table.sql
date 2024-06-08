CREATE SEQUENCE sq_user
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

CREATE TABLE "user" (
    id bigint NOT NULL,
    username varchar (100) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    CONSTRAINT user_pkey PRIMARY KEY (id)
)
