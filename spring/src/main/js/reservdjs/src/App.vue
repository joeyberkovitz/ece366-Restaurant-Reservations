<template>
  <div id="app">
      <md-toolbar class="nav">
          <h3 class="md-title" style="flex: 1;text-align: left;">RESERVD</h3>
          <md-button to="/">Home</md-button>
          <md-button to="/about">About</md-button>
          <span v-show="loggedIn()">
            <md-button to="/restaurant/new">Create Restaurant</md-button>
            <md-button to="/profile">User Profile</md-button>
            <md-button @click="logout()">Logout</md-button>
          </span>
          <md-button to="/login" v-show="!this.loggedIn()">Login</md-button>
      </md-toolbar>
      <router-view class="routerView"/>
  </div>
</template>
<script>
    import * as client from './proto/RestaurantServiceServiceClientPb';

    export default {
        created() {
            console.log("app running");
            const authClient = new client.AuthServiceClient(this.$store.getters.config.host,null,null);
            const restaurantClient = new client.RestaurantServiceClient(this.$store.getters.config.host,null,null);
            this.$store.commit('setAuthClient', authClient);
            this.$store.commit('setRestaurantClient', restaurantClient);
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
