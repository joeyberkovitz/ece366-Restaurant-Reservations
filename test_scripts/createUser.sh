#!/bin/bash

grpcurl -d @ -plaintext localhost:50051 AuthService.createUser <<EOM
{
	"password": "testPassword",
	"user": {
		"username": "testUser1",
		"fname": "testFname",
		"lname": "testLname",
		"points": 100,
		"role": 2,
		"contact": {
			"phone": "2123334144",
			"email": "test@fake.com"
		}
	}
}
EOM