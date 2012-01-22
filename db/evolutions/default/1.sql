# --- !Ups

create table album (
  id             bigint not null primary key,
  name           varchar(255) not null,
  artist         bigint not null,
  releaseDate    timestamp not null,
  genre          int not null,
  nbVotes        int not null,
  hasCover       bool not null
);

create sequence album_seq start with 1000;

create table artist (
  id             bigint not null primary key,
  name           varchar(255) not null
);

create sequence artist_seq start with 1000;

# --- !Downs

drop table if exists album;
drop sequence if exists album_seq;
drop table if exists artist;
drop sequence if exists artist_seq;
