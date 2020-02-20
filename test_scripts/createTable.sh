#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
tableName=$2
restaurantId=$3
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.createTable <<EOM
{
	"target": {
		"id": $restaurantId
	},
	"table": {
		"capacity": 20,
		"label": "$tableName"
	}
}
EOM