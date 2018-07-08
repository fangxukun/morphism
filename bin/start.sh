#!/bin/bash
BASE_PATH=$(cd `dirname $0`; pwd)
export MAVEN_OPTS="-Xms512m -Xmx512m -XX:MaxPermSize=256m"
cd ${BASE_PATH}/../engine-server
mvn jetty:run
