use mysql;

delete from user where User='$$ACE(ACEOPERATOR_SQL_USER)';
flush privileges;
drop database if exists $$ACE(ACEOPERATOR_SQL_DB);
