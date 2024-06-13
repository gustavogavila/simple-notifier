CREATE SEQUENCE sq_outb_mess
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START 1
    CACHE 1
    NO CYCLE;

CREATE TABLE outbox_message (
    id bigint NOT NULL,
    created_at timestamp with time zone NOT NULL,
    content text NOT NULL,
    status varchar(50) NOT NULL,
    destination varchar(255) NOT NULL,
    tentatives integer,
    headers varchar(255),
    CONSTRAINT outb_mess_pkey PRIMARY KEY (id)
)
