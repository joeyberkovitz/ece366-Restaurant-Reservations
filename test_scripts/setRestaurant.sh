#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
id=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.setRestaurant <<EOM
{
	"address": {
		"name": "TEST updated address",
		"latitude": 41.728512,
		"longitude": -71.990257,
		"line1": "42 Cooper Square",
		"city": "New York",
		"state": "NY",
		"zip": "10009"
	},
	"contact": {
		"phone": "2123334145",
		"email": "test2@fake.com"
	},
	"category": {
		"category": 2
	},
	"name": "TEST Restaurant 2",
	"id": $id
}
EOM