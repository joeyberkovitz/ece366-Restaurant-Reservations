#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
reservationId=$2

grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 ReservationService.listReservationUsers <<EOM
{
	"id": $reservationId
}
EOM