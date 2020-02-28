#!/bin/bash

authToken=$1
id=$2
numPeople=$3
resStart=`date --date="$4" +"%s"`
points=$5
restId=$6
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 ReservationService.setReservation <<EOM
{
  "id": $id,
  "startTime": "$resStart",
  "numPeople": "$numPeople",
  "points": "$points",
  "status": "0",
  "restaurant": {
  	"id": $restId
  }
}
EOM
