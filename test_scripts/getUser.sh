#!/bin/bash

authToken=$1
userId=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 UserService.getUser <<EOM
{
  "id": $userId
}
EOM