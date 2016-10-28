#!/bin/sh

if [ -z "$ACE_VERSION" ]
then
	echo Environment variable ACE_VERSION is not set
	exit 1
fi

if [ -n "$ACE_HOME" ]
then
	cd "$ACE_HOME"/ace-docker
fi

rm -rf images/ace-db/work/*
cp -R ../ace-assembly/target/ace-$ACE_VERSION/ace-$ACE_VERSION/.ace/sql/* images/ace-db/work
cd images/ace-db
docker build -t aceoperator/db .

cd ../..
rm -rf images/ace-mail/work/*
cd images/ace-mail
docker build -t aceoperator/mail .

cd ../..
rm -rf images/ace-app/work/*
cp -R ../ace-assembly/target/ace-$ACE_VERSION images/ace-app/work
cd images/ace-app
docker build --build-arg ACE_VERSION=ace-$ACE_VERSION -t aceoperator/app .