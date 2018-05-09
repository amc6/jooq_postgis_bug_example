CREATE SCHEMA postgis;
CREATE EXTENSION postgis SCHEMA postgis;

-- So that postgis types and functions do not have to be qualified.
DO $$
BEGIN
  execute 'alter database ' || current_database() || ' set search_path = public,postgis';
END;
$$;

CREATE TABLE example (
  id BIGSERIAL PRIMARY KEY,
  location GEOGRAPHY(POINT, 4326) NOT NULL
);

CREATE INDEX ON example
USING GIST (location);