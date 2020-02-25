#!/bin/bash

refreshToken=$1
grpcurl -d @ -plaintext localhost:50051 AuthService.refreshToken <<EOM
{
	"refreshToken": "$refreshToken",
	"userAgent": "CHROMIUM FAKE"
}
EOM