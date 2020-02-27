#!/bin/bash
authToken=$1
id=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.getRestaurantsByUser <<EOM
{
	"id": $id
}
EOM