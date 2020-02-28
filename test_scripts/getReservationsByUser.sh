#!/bin/bash
authToken=$1
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 ReservationService.getReservationsByUser <<EOM
{
}
EOM