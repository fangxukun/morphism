#!/bin/bash
SRC_DIR=../engine-client/src/main/java/
DEST_DIR=../engine-client/src/main/java/
protoc -I=${SRC_DIR} --java_out=${DEST_DIR} ${SRC_DIR}/com/vdian/engine/client/netty/protocol/solr.proto