#!/bin/bash

grpcurl -d @ -plaintext localhost:50051 AuthService.authenticate <<EOM
{
	"password": "testPassword",
	"username": "testUser1",
	"userAgent": "CHROMIUM FAKE"
}
EOM
