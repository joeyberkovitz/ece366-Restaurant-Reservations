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
					</div>
					<md-button type="submit">Search</md-button>
				</form>
					</md-card-content>
				</md-card-area>
			<md-card-content>
				<md-list class="md-triple-line">
					<div v-for="(result) in results" :key="result.getRestaurant().getId()">
						<md-list-item>
							<div class="md-list-item-text">
								<span>{{result.getRestaurant().getName()}}</span>
								<span>{{getCategory(result.getRestaurant().getCategory().getCategory())}}</span>
								<span>Available Capacity: {{result.getAvailablecapacity()}}</span>
							</div>
							<md-button class="bold md-primary" v-on:click="createReservation(result.getRestaurant())">Reserve
							</md-button>
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
	import {required, integer, minValue} from "vuelidate/lib/validators";
	import {validationMixin} from "vuelidate";
	import {Category, Reservation} from "../proto/RestaurantService_pb";
	import {ReservationServiceClient, RestaurantServiceClient} from "../proto/RestaurantServiceServiceClientPb";

	export default {
		name: "Search",
		mixins: [validationMixin],
		created() {
			this.client = this.$store.getters.grpc.restaurantClient;
			this.reservationClient = this.$store.getters.grpc.reservationClient;
			const promise = this.client.client.getCategoryList(new GetCategoryRequest(), {});
			promise.on('data', (data) => {
				this.categories.push(data);
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
			client: null,
			reservationClient: null,
			categories: [],
			form: {
				date: null,
				time: null,
				numPeople: 1,
				category: null
			},
			menu2: false,
			results: [],
			showSnackBar: false,
			snackBarMessage: "",
			snackBarDuration: 4000
		}),
		methods: {
			createReservation(restaurant){
				const newReservation = new Reservation();
				newReservation.setRestaurant(restaurant);
				newReservation.setNumpeople(this.form.numPeople);
				newReservation.setStarttime(this.form.date.getTime()/1000 +
					this.timeToSeconds(this.form.time));
				newReservation.setStatus(Reservation.ReservationStatus.OPENED);

				this.reservationClient.client.createReservation(newReservation, {}, (err, response) => {
					console.log(err, response);
					if(err)
						this.snackBarMessage = err.message;
					else
						this.snackBarMessage = "Reservation created, go to my reservations to view it";
					this.showSnackBar = true;
					this.results = [];
					this.validateForm();
				});
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
					this.results = [];
					const search = this.client.client.searchRestaurants(searchRequest, {});
					search.on('data', response => {
						this.results.push(response);
					});
				}
				else
					console.log('invalid')
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