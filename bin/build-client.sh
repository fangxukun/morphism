#!/bin/bash
BASE_PATH=$(cd `dirname $0`; pwd)
cd ${BASE_PATH}/../engine-client
mvn -Dmaven.test.skip clean package install