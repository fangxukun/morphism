#!/bin/bash
BASE_PATH=$(cd `dirname $0`; pwd)
cd ${BASE_PATH}/../
#mvn -Dmaven.test.skip clean package install
mvn -Dmaven.test.skip clean assembly:assembly