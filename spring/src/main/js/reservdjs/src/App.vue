<template>
  <div id="app">
      <md-toolbar class="nav">
          <h3 class="md-title" style="flex: 1;text-align: left;">RESERVD</h3>
          <md-button to="/">Home</md-button>
          <span v-show="loggedIn()">
            <md-button to="/restaurant/new">Create Restaurant</md-button>
            <md-button to="/restaurant/manage/0">Manage Restaurants</md-button>
            <!-- TODO only show if user has restaurants -->
            <md-button to="/reservation">My Reservations</md-button>
            <md-button to="/profile">User Profile</md-button>
            <md-button to="/search">Search</md-button>
            <md-button @click="logout()">Logout</md-button>
          </span>
          <md-button to="/login" v-show="!this.loggedIn()">Login</md-button>
      </md-toolbar>
      <v-app>
        <router-view class="routerView"/>
      </v-app>
  </div>
</template>
<script>
    import * as client from './proto/RestaurantServiceServiceClientPb';
    import {CustomRPCClient} from "@/proto/CustomRPCClient";
    import {RestaurantServiceClient, UserServiceClient} from "./proto/RestaurantServiceServiceClientPb";
    import {ReservationServiceClient} from "@/proto/RestaurantServiceServiceClientPb";
    import {GetCategoryRequest, RefreshRequest, Restaurant} from "@/proto/RestaurantService_pb";
    import store, {UserState} from "@/store";
    import router from "@/router";

    export default {
        created() {
            console.log("app running");
            const authClient = new client.AuthServiceClient(this.$store.getters.config.host,null,null);
            this.$store.commit('setAuthClient', authClient);

            const authInterceptor = function () {
                return null;
            };
            authInterceptor.prototype.intercept = function (request, invoker) {
                const InterceptedStream = function(stream) {
                    this.stream = stream;
                    this.cbKeys = ["data", "error", "status", "metadata", "end"];
                    this.cb = {};
                    for(let i = 0; i < this.cbKeys.length; i++){
                        this.cb[this.cbKeys[i]] = [];
                        if(this.cbKeys[i] == "error"){
                            this.stream.on("error", (err) => {
                                if(err.code == 16){
                                    console.log("Intercepted AUTH ERROR");
                                    const authClient = store.getters.grpc.authClient;
                                    const refreshRequest = new RefreshRequest();
                                    refreshRequest.setRefreshtoken(store.getters.user.refreshToken);
                                    refreshRequest.setUseragent(navigator.userAgent);
                                    authClient.refreshToken(refreshRequest, {}, (err, resp) => {
                                        if(err){
                                            console.log("Failed to refresh");
                                            store.commit('storeUserState', null);
                                            router.push("/login");
                                        }
                                        else{
                                            const userState = new UserState();
                                            userState.refreshToken = resp.getRefreshtoken();
                                            userState.authToken = resp.getAuthtoken();
                                            store.commit('storeUserState', userState);
                                            console.log("Rerunning request");
                                            console.log(this);
                                            const meta = request.getMetadata();
                                            meta['Authorization'] = store.getters.user.authToken;
                                            const s2 = new InterceptedStream(invoker(request));
                                            s2.on('data', this.cb['data'][0]);
                                            console.log("S2");
                                            console.log(s2);
                                            console.log(this.cb['data'][0]);
                                        }
                                    });
                                }
                                else{
                                    console.log("Intercepted error code: " + err.code);
                                    for(let j = 0; j < this.cb["error"].length; j++){
                                        this.cb['error'][j](err);
                                    }
                                }
                            })
                        }
                        else{
                            this.stream.on(this.cbKeys[i], (response) => {
                                console.log("Running CBs for " + this.cbKeys[i]);
                                for(let j = 0; j < this.cb[this.cbKeys[i]].length; j++){
                                    console.log(j);
                                    this.cb[this.cbKeys[i]][j](response);
                                }
                            })
                        }
                    }
                };
                InterceptedStream.prototype.on = function(eventType, callback) {
                    this.cb[eventType].push(callback);
                    return this;
                };

                const meta = request.getMetadata();
                meta['Authorization'] = store.getters.user.authToken;
                return new InterceptedStream(invoker(request));
            }


            const restaurantClient = new RestaurantServiceClient(this.$store.getters.config.host, null, {'streamInterceptors': [new authInterceptor()]});
            const userClient = new UserServiceClient(this.$store.getters.config.host, null, {'streamInterceptors': [new authInterceptor()]});
            const reservationClient = new ReservationServiceClient(this.$store.getters.config.host, null, {'streamInterceptors': [new authInterceptor()]});
            this.$store.commit('setRestaurantClient', restaurantClient);
            this.$store.commit('setReservationClient', reservationClient);
            this.$store.commit('setUserClient', userClient);
        },
        methods: {
            loggedIn(){
                return !!this.$store.getters.user&&!!this.$store.getters.user.authToken;
            },
            logout(){
                this.$store.commit('storeUserState', null);
                this.$router.push("/login");
            }
        }
    }
</script>
<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

.nav{
    position: fixed !important;
    top: 0;
    z-index: 3;
}

.routerView{
    margin-top:100px;
}
</style>
