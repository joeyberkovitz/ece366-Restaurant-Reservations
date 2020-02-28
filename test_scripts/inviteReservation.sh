#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
reservationId=$2
userId=$3

grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 ReservationService.inviteReservation <<EOM
{
	"reservation": {
		"id": $reservationId
	},
	"user": {
		"id": $userId
	}
}
EOM