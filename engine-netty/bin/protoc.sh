#!/bin/bash
SRC_DIR=../src/main/java
DEST_DIR=../src/main/java
protoc -I=${SRC_DIR} --java_out=${DEST_DIR} ${SRC_DIR}/com/vdian/search/netty/protocol/solr.proto