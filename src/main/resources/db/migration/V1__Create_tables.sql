CREATE TABLE LINKS_ALREADY_PROCESSED (
link varchar(1000)
);

CREATE TABLE LINKS_TO_BE_PROCESSED (
link varchar(1000)
);

CREATE TABLE NEWS (
id bigint PRIMARY KEY auto_increment,
title text,
content text,
url varchar(1000),
created_at timestamp,
updated_at timestamp
);

