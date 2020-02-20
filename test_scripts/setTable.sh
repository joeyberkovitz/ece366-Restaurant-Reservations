#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
tableId=$2
tableName=$3
capacity=$4
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.setTable <<EOM
{
	"id": $tableId
	"capacity": $capacity,
	"label": "$tableName"
}
EOM