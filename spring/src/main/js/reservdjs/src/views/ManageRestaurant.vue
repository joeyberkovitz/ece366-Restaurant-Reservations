<template>
	<div class="centered">
		<div class="md-layout md-alignment-center-center">
			<md-card class="md-layout-item md-size-50 md-small-size-50">
				<md-card-header>
					Jump to restaurant
				</md-card-header>
				<md-card-content>
					<md-field>
				        	<label for="restaurant">My restaurants</label>
					        <md-select name="restaurant" id="restaurantSel"
					                v-on:md-selected="$root.$emit('restaurant', $event)" v-if="! resetting">
						        <md-option v-for="(restaurant) in restaurants"
					        	        :key="restaurant.getId()"
		        			        	:value="restaurant.getId()">{{restaurant.getName()}}</md-option>
				        	</md-select>
					</md-field>
				</md-card-content>
			</md-card>
		</div>
		<RestaurantForm :client="this.client"
		                :categories="this.categories"
						:create="false"
		                button="Set Restaurant"
		                :canEdit="this.canEdit"
						@set="loadRest()"/>
		<ManageUsers :client="this.client"
					 v-if="this.canEdit"/>
		<ManageTables :client="this.client"
					  @load="load(filterData)"
		              v-if="this.canEdit"/>
		<ReservationList :reservations="this.reservations"
						 @load="load($event)"
						 v-if="this.canEdit"
						 :restView="true"/>
		<md-snackbar md-position="center" :md-duration="snackBarDuration"
					 :md-active.sync="showSnackBar" md-persistent>
			<span>{{snackBarMessage}}</span>
		</md-snackbar>
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
		resetting: false,
		canEdit: false,
		userId: 0,
		filterData: null,
		showSnackBar: false,
		snackBarMessage: "",
		snackBarDuration: 4000
	}),
	created() {
		this.client = this.$store.getters.grpc.restaurantClient;
		this.userId = JSON.parse(atob(this.$store.getters.user.authToken.split('.')[1])).sub;
		const promise = this.client.getCategoryList(new GetCategoryRequest(), {}, err => {
			if(err) {
				console.log(err);
				this.snackBarMessage = err.message;
				this.showSnackBar = true;
			}
		});
		promise.on('data', (data) => {
			this.categories.push(data);
		});

		this.checkEdit();
	},
	mounted() {
		this.$root.$on('restaurant', (event) => {
			console.log(event);
			const id = event;
			if(id != this.$route.params.id) {
				this.$router.push({params: {id:id}})
			}
		});
	},
	methods: {
		loadRest() {
			this.restaurants = [];
			const user = new User();
			user.setId(this.userId);
			const promise2 = this.client.getRestaurantsByUser(user, {},
					err => { if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
					});
			promise2.on('data', (data) => {
				this.restaurants.push(data.getRestaurant());
			});
		},
		load(filterData) {
			this.filterData = filterData;
			this.reservations = [];
			const resClient = this.$store.getters.grpc.reservationClient;
			const request = new ReservationRestaurantRequest();
			request.setBegintime(filterData.date.getTime()/1000);
			request.setFuturetime(filterData.futureDate.getTime()/1000);
			request.setStatus(filterData.status);
			const restaurant = new Restaurant();
			restaurant.setId(this.$route.params.id);
			request.setRestaurant(restaurant);
			const promise1 = resClient.getReservationsByRestaurant(request,{}, err => {
				if(err) {
					console.log(err);
					this.snackBarMessage = err.message;
					this.showSnackBar = true;
				}
			});
			promise1.on('data', (data1) => {
				const usersR = [];
				const promise2 = resClient.listReservationUsers(data1, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
				});
				promise2.on('data', (data2) => {
					usersR.push(data2);
				});
				const tables = [];
				const promise3 = resClient.getReservationTables(data1, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
				});
				promise3.on('data', (data3) => {
					tables.push(data3);
				});
				this.reservations.push({details: data1,
					invites: usersR, tables: tables});
			});
		},
		checkEdit() {
			this.canEdit = false;
			console.log(this.$route.params.id);
			if (this.$route.params.id != 0) {
				const req = new Restaurant();
				req.setId(this.$route.params.id);
				const promise3 = this.client.getUsersByRestaurant(req, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
				});
				promise3.on('data', (data) => {
					if(data.getUser().getId() == this.userId) {
						this.canEdit = true;
					}
				});
			}
			else {
				this.restaurants = [];
				this.reset();
			}
			this.loadRest();
		},
		// due to md-select bug
		reset() {
			this.resetting = true
			setTimeout(() => {this.resetting = false}, 100)
		}
	},
	watch: {
		$route(to, from) {
			this.checkEdit();
		}
	},
}
</script>

<style scoped>

</style>
