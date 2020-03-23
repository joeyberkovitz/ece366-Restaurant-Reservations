import Vue from 'vue'
import Vuex from 'vuex'
import VuexPersist from 'vuex-persist'

const vuexPersist = new VuexPersist({
  key: 'reservdjs',
  storage: window.localStorage
});

Vue.use(Vuex);

export default new Vuex.Store({
  plugins: [vuexPersist.plugin],
  state: {
    version: '1.0.0',
    authToken: null,
    refreshToken: null,
    expirationTime: 0
  },
  mutations: {
  },
  actions: {
  },
  modules: {
  }
})
