
\connect postgres

-- ═══════════════════════════════════════════════════════════════════
-- Create Application Databases
-- ═══════════════════════════════════════════════════════════════════

CREATE DATABASE expense_db
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

CREATE DATABASE auth_service_db
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;

-- ═══════════════════════════════════════════════════════════════════
-- Create Application Users (Optional)
-- ═══════════════════════════════════════════════════════════════════

-- Create app_user with permissions on both databases
CREATE USER app_user WITH PASSWORD 'app_password';

GRANT ALL PRIVILEGES ON DATABASE expense_db TO app_user;
GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO app_user;

-- ═══════════════════════════════════════════════════════════════════
-- Verify Databases Created
-- ═══════════════════════════════════════════════════════════════════


\l

-- ═══════════════════════════════════════════════════════════════════
-- Optional: Create Basic Schema in auth_service_db (if not using Flyway)
-- ═══════════════════════════════════════════════════════════════════

\connect auth_service_db

-- Tables will be created by:
-- 1. Flyway migrations (src/main/resources/db/migration/), OR
-- 2. Hibernate DDL generation (if JPA is configured), OR
-- 3. Manual SQL scripts



\connect expense_db
