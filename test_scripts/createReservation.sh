#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
resStart=`date --date="$2" +"%s"`
restaurantId=$3
numPeople=$4

grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 ReservationService.createReservation <<EOM
{
	"startTime": $resStart,
	"restaurant": {
		"id": $restaurantId
	},
	"numPeople": $numPeople
}
EOM