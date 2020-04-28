import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'

Vue.use(VueRouter);

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/restaurant/new',
    name: 'New Restaurant',
    component: () => import('../views/NewRestaurant.vue')
  },
  {
    path: '/restaurant/manage/:id',
    name: 'Manage Restaurant',
    component: () => import('../views/ManageRestaurant.vue')
  },
  {
    path: '/profile',
    name: 'Manage User',
    component: () => import('../views/ManageUser.vue')
  },
  {
    path: '/reservation',
    name: 'My Reservations',
    component: () => import('../views/MyReservations.vue')
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('../views/Search.vue')
  }
];

const router = new VueRouter({
  mode: 'hash',
  base: process.env.BASE_URL,
  routes
});

export default router
