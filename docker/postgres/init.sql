-- =============================================================
-- CineBook - PostgreSQL Initialization Script
--
-- Runs once on first container start (when data volume is empty).
-- Creates a dedicated database for each microservice.
-- Each service connects only to its own database (DB-per-service pattern).
-- =============================================================

CREATE DATABASE cinebook_users;
CREATE DATABASE cinebook_movies;
CREATE DATABASE cinebook_bookings;
