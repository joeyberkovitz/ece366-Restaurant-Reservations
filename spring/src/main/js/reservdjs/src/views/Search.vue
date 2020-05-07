<template>
	<div class="centered md-layout md-alignment-top-center">
		<md-card class="md-layout-item md-size-75 md-big-size-75">

				<md-card-area md-inset>
					<md-card-content>
				<form @submit.prevent="validateForm">
					<div class="md-layout">
							<md-datepicker v-model="form.date" class="md-layout-item md-size-50" :class="getValidationClass('date')"/>
							<div class="md-layout-item md-size-50">
							<v-menu
									v-model="menu2"
									:close-on-content-click="false"
									:nudge-right="40"
									transition="scale-transition"
									offset-y
									max-width="290px"
									min-width="290px"
							>
								<template v-slot:activator="{ on }">
									<md-field :class="getValidationClass('time')">
										<md-icon>query_builder</md-icon>
										<md-input v-model="form.time" type="time" v-on="on"></md-input>
									</md-field>
								</template>
								<v-time-picker
										:allowed-minutes="[0, 30]"
										v-if="menu2"
										v-model="form.time"
										full-width
								></v-time-picker>
							</v-menu>
							</div>
					</div>
					<div class="md-layout">
							<md-field class="md-layout-item md-size-50" :class="getValidationClass('numPeople')">
								<md-icon>people</md-icon>
								<md-input type="number" min="1" v-model="form.numPeople"></md-input>
							</md-field>
							<md-field class="md-layout-item padLeft">
								<label for="category">Cuisine</label>
								<md-select name="category" id="category" v-model="form.category">
									<md-option v-for="(category) in categories"
									           :key="category.getCategory()"
									           :value="category.getCategory()">{{category.getName()}}</md-option>
								</md-select>
							</md-field>
							<md-field class="md-layout-item padLeft" v-if="gotLocation">
								<label for="radius">Search Radius</label>
								<md-select name="radius" id="radius" v-model="form.radius">
									<md-option value="0">Any</md-option>
									<md-option value="1">1 mile</md-option>
									<md-option value="5">5 miles</md-option>
									<md-option value="10">10 miles</md-option>
									<md-option value="50">50 miles</md-option>
									<md-option value="100">100 miles</md-option>
								</md-select>
							</md-field>
					</div>
					<md-button type="submit">Search</md-button>
				</form>
					</md-card-content>
				</md-card-area>
			<md-card-content>
				<md-list class="md-triple-line">
					<div v-for="restId in rests" :key="restId">
						<md-list-item>
							<div class="md-list-item-text">
								<span>{{results[restId][0].getRestaurant().getId()}} -
									{{results[restId][0].getRestaurant().getName()}} - <span style="color: slategray;"
									                                                         v-if="gotLocation">
									{{compDist(results[restId][0].getRestaurant())}} miles</span></span>
								<span>{{getCategory(results[restId][0].getRestaurant().getCategory().getCategory())
									}}</span>
								<md-button style="width:150px;color:#448aff;" class="bold md-primary" v-on:click="view(restId)">View Restaurant</md-button>
								<div>
									<md-button v-for="(time) in results[restId]" :key="time.getAvailabledate()"
									           style="width:150px;color:#448aff;" class="bold md-primary"
											v-on:click="createReservation(time.getRestaurant(), time.getAvailabledate())">
										{{time.getAvailabledate() | formatDate}} ({{time.getAvailablecapacity()}})
									</md-button>
								</div>
							</div>
						</md-list-item>
						<md-divider></md-divider>
					</div>
				</md-list>
				<md-snackbar md-position="center" :md-duration="snackBarDuration"
				             :md-active.sync="showSnackBar" md-persistent>
					<span>{{snackBarMessage}}</span>
				</md-snackbar>
			</md-card-content>
		</md-card>
	</div>
</template>

<script>
	import {GetCategoryRequest, RestaurantSearchRequest} from "@/proto/RestaurantService_pb";
	import {integer, minValue, required} from "vuelidate/lib/validators";
	import {validationMixin} from "vuelidate";
	import {Category, Reservation} from "../proto/RestaurantService_pb";
	import moment from "moment";
	//import {ReservationServiceClient, RestaurantServiceClient} from "../proto/RestaurantServiceServiceClientPb";

	export default {
		name: "Search",
		mixins: [validationMixin],
		created() {
			this.client = this.$store.getters.grpc.restaurantClient;
			this.reservationClient = this.$store.getters.grpc.reservationClient;
			const promise = this.client.getCategoryList(new GetCategoryRequest(), {});
			promise.on('data', (data) => {
				this.categories.push(data);
			});

			navigator.geolocation.getCurrentPosition(data => {
				if(data && data.coords && data.coords.latitude) {
					this.gotLocation = true;
					this.form.lat = data.coords.latitude;
					this.form.long = data.coords.longitude;
				}
			});
		},
		validations: {
			form: {
				date: {
					required
				},
				time: {
					required
				},
				numPeople: {
					required,
					integer,
					minValue: minValue(1)
				}
			}
		},
		data: () => ({
			gotLocation: false,
			client: null,
			reservationClient: null,
			categories: [],
			form: {
				date: null,
				time: null,
				numPeople: 1,
				category: null,
				lat: 0,
				long: 0,
				radius: 0
			},
			menu2: false,
			results: {},
			rests: [],
			showSnackBar: false,
			snackBarMessage: "",
			snackBarDuration: 4000
		}),
		filters: {
			formatDate: function(value) {
				if (value) {
					return moment(value*1000).format('LT')
				}
			}
		},
		methods: {
			createReservation(restaurant, time){
				console.log(restaurant);
				console.log(time);
				const newReservation = new Reservation();
				newReservation.setRestaurant(restaurant);
				newReservation.setNumpeople(this.form.numPeople);
				newReservation.setStarttime(time);
				newReservation.setStatus(Reservation.ReservationStatus.OPENED);

				this.reservationClient.createReservation(newReservation, {}, (err, response) => {
					console.log(err, response);
					if(err)
						this.snackBarMessage = err.message;
					else
						this.snackBarMessage = "Reservation created, go to my reservations to view it";
					this.showSnackBar = true;
					this.results = {};
					this.validateForm();
				});
			},
			compDist(restaurant){
				if(restaurant.dist)
					return restaurant.dist;

				const address = restaurant.getAddress();
				const l1 = this.form.lat * Math.PI/180, l2 = address.getLatitude() * Math.PI/180, d1 =
					(address.getLongitude() - this.form.long) * Math.PI/180, R = 3961;
				const dist = Math.round(
					Math.acos(Math.sin(l1) * Math.sin(l2) + Math.cos(l1) * Math.cos(l2) * Math.cos(d1)) * R
				);

				restaurant.dist = dist;
				return dist;
			},
			getCategory(categoryId){
				for(let i = 0; i < this.categories.length; i++){
					if(this.categories[i].getCategory() === categoryId)
						return this.categories[i].getName();
				}
				return null;
			},
			timeToSeconds(timeStr){
				const time= timeStr.split(":");
				return time[0]*3600 + time[1]*60;
			},
			getValidationClass(elem){
				const field = this.$v.form[elem];
				if(field)
					return {
						'md-invalid': field.$invalid && field.$dirty
					};
			},
			validateForm() {
				this.$v.$touch();
				if (!this.$v.$invalid) {
					const date = this.form.date.getTime()/1000 +
						this.timeToSeconds(this.form.time);
					const searchRequest = new RestaurantSearchRequest();
					searchRequest.setNumpeople(this.form.numPeople);
					searchRequest.setRequesteddate(date);
					if(this.form.category > 0) {
						const category = new Category();
						category.setCategory(this.form.category);
						searchRequest.setCategory(category);
					}
					searchRequest.setLat(this.form.lat);
					searchRequest.setLong(this.form.long);
					searchRequest.setNummiles(this.form.radius);
					this.results = {};
					this.rests = [];
					const search = this.client.searchRestaurants(searchRequest, {});
					search.on('data', response => {
						const restId = response.getRestaurant().getId();
						if(!this.results[restId]) {
							this.results[restId] = [];
							this.rests.push(restId);
						}
						this.results[restId].push(response);
						this.rests.sort((a, b) => {
							const da = this.compDist(this.results[a][0].getRestaurant());
							const db = this.compDist(this.results[b][0].getRestaurant());
							if(da < db) return -1;
							else if(da > db) return 1;
							else return 0;
						});
					});
				}
				else
					console.log('invalid')
			},
			view(restId) {
				this.$router.push("/restaurant/view/" + restId);
			}
		}
	}
</script>

<style scoped>
	.centered{
		height: 100vh
	}
	.md-menu-content-custom{
		overflow: visible;
		height: 100%;
	}
	.padLeft{
		margin-left: 10px;
		flex: 50;
	}
</style>