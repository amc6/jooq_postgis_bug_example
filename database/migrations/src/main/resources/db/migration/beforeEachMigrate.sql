-- For some reason, flyway doesn't pick up changes to the search path while running migrations.
-- This makes sure it is set before the necessary changes. See https://github.com/flyway/flyway/issues/1379
SET search_path = public, postgis