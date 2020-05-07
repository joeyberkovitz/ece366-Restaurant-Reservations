import Vue from 'vue'
import Vuex from 'vuex'
import {
    AuthServiceClient,
    ReservationServiceClient,
    RestaurantServiceClient, UserServiceClient
} from "@/proto/RestaurantServiceServiceClientPb";
import VuexPersistence from "vuex-persist";
import {CustomRPCClient} from "@/proto/CustomRPCClient";

export class UserState {
    public authToken!: string;
    public refreshToken!: string;
}

export class ConfigState {
    public host = process.env.VUE_APP_GRPC_HOST;
}

export class GrpcState {
    public authClient!: AuthServiceClient;
    public restaurantClient!: RestaurantServiceClient;
    public reservationClient!: ReservationServiceClient;
    public userClient!: UserServiceClient;
}

export interface State {
  user: UserState;
  grpc: GrpcState;
  config: ConfigState;
}


const vuexPersist = new VuexPersistence<State>({
      key: 'reservdjs',
      storage: window.localStorage,
      reducer: (state) => ({
        user: state.user
      })
});

Vue.use(Vuex);

export default new Vuex.Store<State>({
  plugins: [vuexPersist.plugin],
  getters: {
      grpc: state => state.grpc,
      config: state => state.config,
      user: state => state.user
  },
  state: {
      user: new UserState(),
      grpc: new GrpcState(),
      config: new ConfigState()
  },
  mutations: {
      setAuthClient(state, payload: AuthServiceClient){
          state.grpc.authClient = payload
      },
      setRestaurantClient(state, payload: RestaurantServiceClient){
          state.grpc.restaurantClient = payload
      },
      setReservationClient(state, payload: ReservationServiceClient){
          state.grpc.reservationClient = payload
      },
      setUserClient(state, payload: UserServiceClient){
          state.grpc.userClient = payload
      },
      storeUserState(state, payload: UserState){
          state.user = payload;
      }
  },
  actions: {
  },
  modules: {
  }
})
