-- Flyway migration V3: Rename _user table to "user"
-- PostgreSQL 17 compatible

ALTER TABLE IF EXISTS _user RENAME TO "user";
ALTER SEQUENCE IF EXISTS _user_id_seq RENAME TO "user_id_seq";
ALTER TABLE IF EXISTS "user" RENAME CONSTRAINT "_user_pkey" TO "user_pkey";
