#!/bin/bash

authToken=$1
tableId=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.getTableById <<EOM
{
	"id": $tableId
}
EOM