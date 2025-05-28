create table users(
    id BIGSERIAL not null primary key,
    email_address varchar(255) not null unique,
    username varchar(50) not null,
    information varchar(1000)
);