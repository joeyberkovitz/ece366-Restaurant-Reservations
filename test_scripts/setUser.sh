#!/bin/bash

authToken=$1
id=$2
grpcurl -H "Authorization:$authToken" -d @ -plaintext localhost:50051 UserService.setUser <<EOM
{
  "id": $id,
  "username": "testUser1",
	"fname": "testFnameone",
	"lname": "testLnameone",
	"points": 100,
	"role": 2,
	"contact": {
	  "id": 0,
		"phone": "7183334144",
		"email": "test2@fake.com"
	}
}
EOM
