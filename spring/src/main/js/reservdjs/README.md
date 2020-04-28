# reservdjs

## Project setup
```
npm run build-grpc
npm install
```
- Create `.env` file in `reservdjs` directory with key: VUE_APP_GRPC_HOST and value = `http://hostname:GRPC_PROXY_PORT`

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