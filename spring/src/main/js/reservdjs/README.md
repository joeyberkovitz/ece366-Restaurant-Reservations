# reservdjs

## Project setup
```
npm run build-grpc
npm install
```

### Running Project
```
# Run web-app
npm run serve
# Run gRPC Proxy
grpcwebproxy --backend_addr=localhost:50051 --run_tls_server=false --allow_all_origins
# Run gRPC Server
gradle clean build run
```

### Compiles and minifies for production
```
npm run build
```