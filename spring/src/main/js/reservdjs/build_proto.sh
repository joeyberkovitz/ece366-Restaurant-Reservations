#!/bin/sh
cdDir=$(dirname 0)/src/proto
cd $cdDir
protoc -I ../../../../../../../grpc/src/main/proto ../../../../../../../grpc/src/main/proto/RestaurantService.proto  --grpc-web_out=mode=grpcweb,import_style=typescript:. --js_out=import_style=commonjs:.
