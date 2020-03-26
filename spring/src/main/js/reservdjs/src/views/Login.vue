<template>
	<div class="centered">
			<form novalidate class="md-layout md-alignment-center-center" @submit.prevent="validateForm">
				<md-card class="md-layout-item md-size-50 md-small-size-50">
					<md-card-header>
						<div class="md-title">
							{{type}}
						</div>
					</md-card-header>
					<md-card-content>
						<span v-if="type === 'Register'">
							<div class="md-layout md-gutter">
								<div class="md-layout-item md-size-50 md-small-size-100">
									<md-field :class="getValidationClass('firstName')">
										<label for="firstName">First Name</label>
										<md-input name="firstName" id="firstName" v-model="form.firstName"/>
										<span class="md-error" v-if="!$v.form.firstName.required">First Name is
											required</span>
									</md-field>
								</div>
								<div class="md-layout-item md-size-50 md-small-size-100">
									<md-field :class="getValidationClass('lastName')">
										<label for="lastName">Last Name</label>
										<md-input name="lastName" id="lastName" v-model="form.lastName"/>
										<span class="md-error" v-if="!$v.form.lastName.required">Last Name is
											required</span>
									</md-field>
								</div>
							</div>
							<md-field :class="getValidationClass('phone')">
								<label for="phone">Phone Number</label>
								<md-input name="phone" id="phone" type="tel" v-model="form.phone"/>
								<span class="md-error" v-if="!$v.form.phone.required">Phone number is
									required</span>
								<span class="md-error" v-if="!$v.form.phone.numeric">Phone number may only
									consist of numbers</span>
							</md-field>
							<md-field :class="getValidationClass('email')">
								<label for="email">Email</label>
								<md-input name="email" id="email" type="email" v-model="form.email"/>
								<span class="md-error" v-if="!$v.form.email.required">Email address is
									required</span>
								<span class="md-error" v-if="!$v.form.email.email">Invalid email address</span>
							</md-field>
						</span>
						<md-field :class="getValidationClass('username')">
							<label for="username">Username</label>
							<md-input name="username" id="username" v-model="form.username" />
							<span class="md-error" v-if="!$v.form.username.required">Username is required</span>
						</md-field>
						<md-field :class="getValidationClass('password')" md-toggle-password>
							<label for="password">Password</label>
							<md-input name="password" id="password" type="password" v-model="form.password" />
							<span class="md-error" v-if="!$v.form.password.required">Password is required</span>
						</md-field>
						<md-snackbar md-position="center" :md-duration="snackBarDuration"
						             :md-active.sync="showSnackBar" md-persistent>
							<span>{{snackBarMessage}}</span>
						</md-snackbar>
					</md-card-content>
					<md-card-actions md-alignment="space-between">
						<md-button type="button" class="button" @click="type = 'Register'" v-show="type !==	'Register'">No Account? Register</md-button>
						<md-button type="submit" class="button md-primary md-raised" :disabled="sending">{{type}}</md-button>
					</md-card-actions>
				</md-card>

			</form>
	</div>
</template>

<script>
	import {validationMixin} from "vuelidate";
	import {required, email, numeric} from 'vuelidate/lib/validators';
	import {AuthRequest, Contact, CreateUserRequest, User} from "../proto/RestaurantService_pb";
	import {UserState} from "../store";

	export default {
		name: "Login",
		mixins: [validationMixin],
		data: () => ({
			type: 'Login',
			form: {
				username: null,
				password: null,
				firstName: null,
				lastName: null,
				email: null,
				phone: null
			},
			showSnackBar: false,
			snackBarMessage: "",
			snackBarDuration: 4000,
			sending: false
		}),
		validations(){
			const ret = {
				form: {
					username: {
						required
					},
					password: {
						required
					}
				}
			};
			if(this.type === 'Register'){
				ret.form = {...ret.form, ...{
						firstName: {
							required
						},
						lastName:{
							required
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
				};
			}
			return ret;
		},
		methods: {
			//Todo: if logged in, redirect to home page
			getValidationClass(elem){
				const field = this.$v.form[elem];
				if(field)
					return {
						'md-invalid': field.$invalid && field.$dirty
					};
			},
			validateForm () {
				this.$v.$touch();

				if(!this.$v.$invalid) {
					this.sending = true;
					if(this.type === "Login")
						this.authenticate();
					else if(this.type === 'Register')
						this.register();
					else
						console.log("Unknown type: " + this.type);
				}
				else {
					this.snackBarMessage = "Invalid form";
					this.showSnackBar = true;
				}
			},
			authenticate(){
				const client = this.$store.getters.grpc.authClient;
				const authRequest = new AuthRequest();
				authRequest.setUsername(this.form.username);
				authRequest.setPassword(this.form.password);
				authRequest.setUseragent(navigator.userAgent);

				client.authenticate(authRequest, {},
					(err, resp) => {
						this.sending = false;
						console.log(err);
						console.log(resp);
						if(!err){
							const userState = new UserState();
							userState.authToken = resp.getAuthtoken();
							userState.refreshToken = resp.getRefreshtoken();

							this.$store.commit('storeUserState', userState);
							//Todo: redirect somewhere
						}
						else{
							this.snackBarMessage = err.message;
							this.showSnackBar = true;
						}
					}
				);
			},
			register(){
				const client = this.$store.getters.grpc.authClient;
				const createUserRequest = new CreateUserRequest();
				const user = new User();
				const contact = new Contact();
				contact.setEmail(this.form.email);
				contact.setPhone(this.form.phone);
				user.setFname(this.form.firstName);
				user.setLname(this.form.lastName);
				user.setUsername(this.form.username);
				user.setContact(contact);
				createUserRequest.setUser(user);
				createUserRequest.setPassword(this.form.password);
				client.createUser(createUserRequest, {}, (err, response) => {
					this.sending = false;
					if(err){
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
					else{
						this.snackBarMessage = "User created, logging in";
						this.showSnackBar = true;
						this.authenticate();
					}
				});
			}
		}
	};
</script>

<style scoped lang="scss">
	.centered{
		display: flex;
		align-items: center;
		justify-content: center;
		position: relative;
		height: 100vh;
	}
	form{
		width: 100%;
		max-width: 1000px;
		padding: 100px;
		position: relative;
	}
	.md-card{
		padding: 20px;
	}
</style>