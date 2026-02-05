CREATE TABLE IF NOT EXISTS node
(
    ident                TEXT                                      NOT NULL,
    gruppe_id            TEXT                                      NOT NULL,
    gruppe_type          TEXT                                      NOT NULL,
    id                   TEXT                                      NOT NULL,
    type_id              TEXT                                      NOT NULL,
    PRIMARY KEY (ident, gruppe_id, gruppe_type, id, type_id)
);

create index if not exists idx_node_ident on node (ident);
create index if not exists idx_node_id on node (id);