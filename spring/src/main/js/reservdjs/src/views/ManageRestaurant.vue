<template>
	<div class="centered">
		<div class="md-layout md-alignment-center-center">
			<md-card class="md-layout-item md-size-50 md-small-size-50">
		        	<label for="restaurant">My restaurants</label>
			        <select name="restaurant" id="restaurantSel"
			                v-on:change="$root.$emit('restaurant', $event)">
			                <option selected key="0" value="0">---</option>
				        <option v-for="(restaurant) in restaurants"
			        	        :key="restaurant.getId()"
        			        	:value="restaurant.getId()">{{restaurant.getName()}}</option>
		        	</select>
			</md-card>
		</div>
		<RestaurantForm :client="this.client"
		                :categories="this.categories"
		                button="Set Restaurant"/>
                <ManageUsers :client="this.client"/>
		<ManageTables :client="this.client"/>
	        <ReservationList :reservations="this.reservations"
	                         @load="load($event)"/>
	</div>
</template>

<script>
import RestaurantForm from '@/components/RestaurantForm.vue'
import ManageUsers from '@/components/ManageUsers.vue'
import ManageTables from '@/components/ManageTables.vue'
import ReservationList from "../components/ReservationList";
import {GetCategoryRequest, User, ReservationRestaurantRequest, Restaurant} from "../proto/RestaurantService_pb";
import {CustomRPCClient} from "../proto/CustomRPCClient";
import {ReservationServiceClient} from "../proto/RestaurantServiceServiceClientPb";

export default {
	name: "SetRestaurant",
	components: {
		RestaurantForm,
		ManageUsers,
		ManageTables,
		ReservationList
	},
	data: () => ({
		client: null,
		categories: [],
		restaurants: [],
		users: [],
		reservations: [],
	}),
	created() {
		this.client = this.$store.getters.grpc.restaurantClient;
		const promise = this.client.client.getCategoryList(new GetCategoryRequest(), {}, err => {
			console.log(err);
		});
		promise.on('data', (data) => {
			this.categories.push(data);
		});
		const user = new User();
		user.setId(JSON.parse(atob(this.$store.getters.user.authToken.split('.')[1])).sub);
		const promise2 = this.client.client.getRestaurantsByUser(user, {},
		        err => { console.log(err); });
	        promise2.on('data', (data) => {
	                this.restaurants.push(data.getRestaurant());
	        });
	},
	mounted() {
		this.$root.$on('restaurant', (event) => {
			const id = event.target.value;
			if(id != this.$route.params.id) {
				this.$router.push({params: {id:id}})
			}
		});
	},
	methods: {
		load(filterData) {
			const resClient = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
	                const request = new ReservationRestaurantRequest();
	                request.setBegintime(filterData.date.getTime()/1000);
	                request.setFuturetime(filterData.futureDate.getTime()/1000);
	                request.setStatus(filterData.status);
	                const restaurant = new Restaurant();
	                restaurant.setId(this.$route.params.id);
	                request.setRestaurant(restaurant);
	                const promise1 = resClient.client.getReservationsByRestaurant(request,{}, err => {
	                    console.log(err);
	                });
	                promise1.on('data', (data1) => {
	                    const usersR = [];
	                    const promise2 = resClient.client.listReservationUsers(data1, {}, err => {
	                        console.log(err);
	                    });
	                    promise2.on('data', (data2) => {
	                        usersR.push(data2.getFname() + " " + data2.getLname());
	                    });
	                    this.reservations.push({details: data1,
	                        invites: usersR});
	                });
                }
	},
}
</script>

<style scoped>

</style>
