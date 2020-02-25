#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
tableId=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.deleteTable <<EOM
{
	"id": $tableId
}
EOM