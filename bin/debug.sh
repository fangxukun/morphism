#!/bin/bash
BASE_PATH=$(cd `dirname $0`; pwd)
cd ${BASE_PATH}/../engine-sync
mvn clean install

export MAVEN_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
cd ${BASE_PATH}/../engine-server
mvnDebug -Pdevelop clean package jetty:run-exploded
