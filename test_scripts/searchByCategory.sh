#!/bin/bash
# shellcheck disable=SC1007
authToken=$1
categoryId=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 RestaurantService.searchByCategory <<EOM
{
	"category": $categoryId
}
EOM