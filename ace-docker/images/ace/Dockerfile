# Dockerfile for ace
FROM centos
MAINTAINER Amit Chatterjee <amit.chatterjee@quik-j.com>

# Install necessary packages
RUN yum -y install mariadb-server.x86_64 wget.x86_64 tar.x86_64 httpd.x86_64 postfix.x86_64 openssh.x86_64 java-1.8.0-openjdk-devel.x86_64 tomcat.noarch

# Setup mysql root password
RUN systemctl start mariadb; sleep 10; mysqladmin -u root -h 127.0.0.1 password durhamnc; systemctl stop mariadb

# Add default user accounts
RUN useradd -m  -p `openssl passwd becky` becky; useradd -m  -p `openssl passwd amit` amit; useradd -m  -p `openssl passwd operations` operations

