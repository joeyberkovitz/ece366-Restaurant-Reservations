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
					</md-card-content>
					<md-card-actions md-alignment="space-between">
						<md-button type="button" class="button" @click="type = 'Register'" v-show="type !==	'Register'">No Account? Register</md-button>
						<md-button type="submit" class="button md-primary md-raised">{{type}}</md-button>
					</md-card-actions>
				</md-card>
			</form>
	</div>
</template>

<script>
	import {validationMixin} from "vuelidate";
	import {required} from 'vuelidate/lib/validators';

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
			},
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
						}
					}
				};
			}
			return ret;
		},
		methods: {
			getValidationClass(elem){
				const field = this.$v.form[elem];
				if(field)
					return {
						'md-invalid': field.$invalid && field.$dirty
					};
			},
			validateForm () {
				this.$v.$touch();

				if(!this.$v.$invalid)
					console.log("Form valid");
				else
					console.log("Invalid form");
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