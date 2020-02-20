#!/bin/bash

authToken=$1
restaurantId=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.getTablesByRestaurant <<EOM
{
	"id": $restaurantId
}
EOM