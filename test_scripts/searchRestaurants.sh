#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
reservationTime=$2
numPeople=$3
categoryId=$4
lat=$5
long=$6
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.searchRestaurants <<EOM
{
	"requestedDate": $reservationTime,
	"numPeople": $numPeople,
	"category": {
		"category": $categoryId
	},
	"lat": $lat,
	"long": $long
}
EOM
