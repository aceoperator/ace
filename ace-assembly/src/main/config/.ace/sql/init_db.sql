use mysql;

GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@localhost IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';
GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@localhost.localdomain IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';
GRANT ALL PRIVILEGES ON webtalk.* TO $$ACE(ACE_SQL_USER)@'%' IDENTIFIED BY '$$ACE(ACE_SQL_PASSWORD)';

CREATE DATABASE webtalk;