#!/bin/bash
BASE_PATH=$(cd `dirname $0`; pwd)
cd ${BASE_PATH}/../engine-common
mvn -Dmaven.test.skip clean install

export MAVEN_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
cd ${BASE_PATH}/../engine-server
mvn -Pdevelop clean package jetty:run-exploded
