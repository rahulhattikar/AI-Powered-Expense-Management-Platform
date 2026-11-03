
\connect postgres



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


CREATE DATABASE notification_service_db
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'C'
    LC_CTYPE = 'C'
    TEMPLATE = template0;


CREATE USER app_user WITH PASSWORD 'app_password';

GRANT ALL PRIVILEGES ON DATABASE expense_db TO app_user;
GRANT ALL PRIVILEGES ON DATABASE auth_service_db TO app_user;


\l

\connect auth_service_db


\connect expense_db

CREATE DATABASE expense_service_db WITH ENCODING = 'UTF8' LC_COLLATE = 'C' LC_CTYPE = 'C' TEMPLATE = template0;
