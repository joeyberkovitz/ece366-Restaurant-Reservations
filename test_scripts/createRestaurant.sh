#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.createRestaurant <<EOM
{
	"address": {
		"name": "TEST address 1",
		"latitude": 40.728512,
		"longitude": -73.990257,
		"line1": "41 Cooper Square",
		"city": "New York",
		"state": "NY",
		"zip": "10008"
	},
	"contact": {
		"phone": "2123334144",
		"email": "test@fake.com"
	},
	"category": {
		"category": 1
	},
	"name": "TEST Restaurant 2"
}
EOM