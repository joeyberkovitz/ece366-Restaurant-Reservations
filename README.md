# ECE366 Restarant Reservation Platform Project
## Setup
### gRPC setup
1. Set up a MySQL instance. Import the schema found in the `sql` folder.
2. Create `grpc/src/main/resources/config.properties` using the example file. Populate it with the
 DB credentials from step 1.
3. `gradle installDist`
4. Run `build/install/grpc/bin/argon-params` and put the keypair and parameters into the above resources file.
5. `gradle :grpc:run` to run gRPC server
### Spring setup
1. Create `spring/src/main/resources/application.properties` using the example file.
2. `gradle :spring:bootRun` to run Spring HTTP server