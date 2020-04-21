<template>
	<form novalidate class="md-layout md-alignment-center-center" @submit.prevent="validateForm">
		<md-card class="md-layout-item md-size-50 md-small-size-50">
			<md-card-header>
				<div class="md-title">
					{{ $route.name }}
				</div>
			</md-card-header>
			<md-card-content>
				<md-field :class="getValidationClass('name')">
					<label for="name">Restaurant Name</label>
					<md-input name="name" id="name" v-model="form.name" :disabled="!this.canEdit"/>
					<span class="md-error" v-if="!$v.form.name.required">Restaurant name is
						required</span>
				</md-field>
				<md-field :class="getValidationClass('rtime')">
					<label for="rtime">Default Reservation Time (Hours)</label>
					<md-input name="rtime" id="rtime" type="number" v-model="form.rtime" :disabled="!this.canEdit"/>
					<span class="md-error" v-if="!$v.form.rtime.required">Default reservation time is
						required</span>
					<span class="md-error" v-if="!$v.form.rtime.numeric">Default reservation time must be a
						number</span>
				</md-field>
				<md-field :class="getValidationClass('category')">
					<label for="category">Category</label>
					<md-select name="category" id="category" v-model="form.category" :disabled="!this.canEdit">
						<md-option v-for="(category) in categories"
						           :key="category.getCategory()"
						           :value="category.getCategory()">{{category.getName()}}</md-option>
					</md-select>
					<span class="md-error" v-if="!$v.form.category.required">Category is
						required</span>
				</md-field>
				<md-field :class="getValidationClass('capacity')">
					<label for="capacity">Minimum Table Capacity (%)</label>
					<md-input name="capacity" id="capacity" type="number" v-model="form.capacity" :disabled="!this.canEdit"/>
					<span class="md-error" v-if="!$v.form.capacity.required">Minimum table capacity is
						required</span>
					<span class="md-error" v-if="!$v.form.capacity.numeric">Minimum table capacity must be a
						number</span>
				</md-field>
				<md-field :class="getAddressClass()">
					<label for="address">Restaurant Address</label>
					<places
							class="md-input"
							v-model="form.address.label"
							id="address"
							@change="val => { form.address.data = val }"
							@focus="form.address.focused = true"
							@blur="form.address.focused = false"
							:options="options"
							:disabled="!this.canEdit">
					</places>
					<span class="md-error" v-if="!$v.form.address.data.latlng.required">Valid address
						required</span>
				</md-field>
				<md-field>
					<label for="address2">Address 2nd Line</label>
					<md-input name="address2" id="address2" type="text" v-model="form.address2" :disabled="!this.canEdit"/>
				</md-field>
				<input type="hidden" v-model="form.addrId" value="0">
				<md-field :class="getValidationClass('phone')">
					<label for="phone">Phone Number</label>
					<md-input name="phone" id="phone" type="tel" v-model="form.phone" :disabled="!this.canEdit"/>
					<span class="md-error" v-if="!$v.form.phone.required">Phone number is
								required</span>
					<span class="md-error" v-if="!$v.form.phone.numeric">Phone number may only
								consist of numbers</span>
				</md-field>
				<md-field :class="getValidationClass('email')">
					<label for="email">Email</label>
					<md-input name="email" id="email" type="email" v-model="form.email" :disabled="!this.canEdit"/>
					<span class="md-error" v-if="!$v.form.email.required">Email address is
								required</span>
					<span class="md-error" v-if="!$v.form.email.email">Invalid email address</span>
				</md-field>
				<input type="hidden" v-model="form.contId" value="0"/>
				<md-snackbar md-position="center" :md-duration="snackBarDuration"
				             :md-active.sync="showSnackBar" md-persistent>
					<span>{{snackBarMessage}}</span>
				</md-snackbar>
			</md-card-content>
			<md-card-actions md-alignment="space-between" v-if="this.canEdit">
				<md-button type="submit" class="button md-primary md-raised" :disabled="sending">{{button}}
					</md-button>
			</md-card-actions>
		</md-card>

	</form>
</template>

<script>
	import {Address, Category, Contact, Restaurant} from "../proto/RestaurantService_pb";
	import {validationMixin} from "vuelidate";
	import {required, numeric, email} from "vuelidate/lib/validators";
	import Places from 'vue-places';

	export default {
		name: 'RestaurantForm',
		props: ['client', 'categories', 'button', 'canEdit'],
		mixins: [validationMixin],
		data: () => ({
		        manageBool: 0,
			type: 'Login',
			options: {
				countries: ['US'],
			},
			form: {
				name: null,
				rtime: 2,
				capacity: 50,
				category: null,
				address: {
					label: null,
					data: {},
					focused: false,
				},
				address2: null,
				email: null,
				phone: null
			},
			showSnackBar: false,
			snackBarMessage: "",
			snackBarDuration: 4000,
			sending: false
		}),
		validations: {
			form: {
				address: {
					data: {
						required,
						latlng: {
							lat: required
						},
					}
				},
				category: {
					required
				},
				name: {
					required
				},
				rtime: {
					required,
					numeric
				},
				capacity: {
					required,
					numeric
				},
				phone: {
					required,
					numeric
				},
				email: {
					required,
					email
				}
			}
		},
		methods: {
			getAddressClass(){
				return {
					'md-has-value': !!this.form.address.label,
					'md-focused': this.form.address.focused,
					'md-invalid': this.$v.form.address.data.latlng.$invalid && this.$v.form.address.data.latlng.$dirty
				};
			},
			getValidationClass(elem){
				const field = this.$v.form[elem];
				if(field)
					return {
						'md-invalid': field.$invalid && field.$dirty
					};
			},
			test(){
				const restaurant = new Restaurant();
				restaurant.setId(1);
				this.client.client.getRestaurant(restaurant, {}, (err, response) => {
					console.log(err, response);
				});
			},
			validateForm(){
				console.log(this.form);
				this.$v.$touch();
				if(!this.$v.$invalid){
					console.log("VALID");
					const restaurant = new Restaurant();
					restaurant.setCapacity(this.form.capacity);
					restaurant.setName(this.form.name);
					restaurant.setRtime(this.form.rtime);

					const category = new Category();
					category.setCategory(this.form.category);
					restaurant.setCategory(category);

					const address = new Address();
					address.setCity(this.form.address.data.city);
					address.setLatitude(this.form.address.data.latlng.lat);
					address.setLongitude(this.form.address.data.latlng.lng);
					address.setLine1(this.form.address.data.name);
					address.setLine2(this.form.address2 ? this.form.address2 : "");
					address.setState(this.form.address.data.administrative);
					address.setZip(this.form.address.data.postcode);
					address.setName(this.form.name);
					address.setId(this.form.addrId);
					restaurant.setAddress(address);

					const contact = new Contact();
					contact.setPhone(this.form.phone);
					contact.setEmail(this.form.email);
					contact.setId(this.form.contId);
					restaurant.setContact(contact);

					if(this.manageBool == 0) {
							console.log(restaurant)
					        this.client.client.createRestaurant(restaurant, {}, (err, response) => {
						        console.log(err, response);
						        if(!err){
							        this.$router.push("/restaurant/manage/" + response.getId());
						        }
					        });
					} else {
						restaurant.setId(this.$route.params.id);
						console.log(this.$route.params.id);
						console.log(restaurant);
						this.client.client.setRestaurant(restaurant, {}, (err, response) => {
							console.log(err, response);
						});
					}

				}
				else{
					console.log("INVALID");
				}
			},
			populate: function(){
				if(this.$route.params.id) {
					this.manageBool = 1;
					const req = new Restaurant();
					req.setId(this.$route.params.id);
					this.client.client.getRestaurant(req, {}, (err, response) => {
						this.form.name = response.getName();
						this.form.capacity = response.getCapacity();
						this.form.name = response.getName();
						this.form.rtime = response.getRtime();

						const category = response.getCategory();
						this.form.category = category.getCategory();

						const address = response.getAddress();
						this.form.address.data = {};
						this.form.address.data.city = address.getCity();
						this.form.address.data.latlng = {};
						this.form.address.data.latlng.lat = address.getLatitude();
						this.form.address.data.latlng.lng = address.getLongitude();
						this.form.address.data.name = address.getLine1();
						this.form.address.label = address.getLine1();
						this.form.address2 = address.getLine2();
						this.form.address.data.administrative = address.getState();
						this.form.address.data.postcode = address.getZip();
						this.form.name = address.getName();
						this.form.addrId = address.getId();

						const contact = response.getContact();
						this.form.phone = contact.getPhone();
						this.form.email = contact.getEmail();
						this.form.contId = contact.getId();
					});
				}
			}
		},
		components: {
			Places
		},
		mounted() {
			this.populate();
		},
		watch: {
			$route(to, from) {
				this.populate();
			}
		},
	}
</script>

<style scoped>

</style>
