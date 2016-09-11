use mysql;

delete from user where User='webtalk';
flush privileges;
drop database if exists webtalk;
