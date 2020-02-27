#!/bin/bash
authToken=$1
restId=$2
userId=$3
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.setRelationship <<EOM
{
	"restaurant": {
		"id": $restId
	},
	"role": 1,
	"user": {
		"id": $userId
	}
}
EOM