<template>
	<div class="md-layout md-alignment-center-center">
		<md-card class="md-layout-item md-size-50 md-small-size-50">
			<md-card-header>
				<div class="md-title">
					Manage Tables
				</div>
			</md-card-header>
			<md-card-content>
				<md-field>
				        <select multiple name="table" id="tableSel"
					        v-on:keyup.delete="delTab(toDelete)"
					        v-model="toDelete">
					        <option v-for="(table) in tables"
				        	        :key="table.getId()"
	        			        	:value="table.getId()">{{table.getLabel()}} ({{table.getCapacity()}})</option>
			        	</select>
		        	</md-field>
				<md-button v-on:click="delTab(toDelete)" class="button md-primary md-raised">Delete
					</md-button>
				<form novalidate @submit.prevent="validateForm">
			        	<md-field :class="getValidationClass('label')">
						<label for="label">New table label</label>
						<md-input name="label" id="label" type="text" v-model="form.label"/>
					</md-field>
			        	<md-field :class="getValidationClass('capacity')">
						<label for="capacity">New table capacity</label>
						<md-input name="capacity" id="capacity" type="number" v-model="form.capacity"/>
						<span class="md-error" v-if="!$v.form.capacity.required">Capacity is
									required</span>
						<span class="md-error" v-if="!$v.form.capacity.numeric">Capacity may only
									consist of numbers</span>
					</md-field>
					<md-button type="submit" class="button md-primary md-raised">Add
						</md-button>
				</form>
			</md-card-content>
		</md-card>

	</div>
</template>

<script>
	import {Restaurant, CreateTableRequest, Table} from "../proto/RestaurantService_pb";
	import {validationMixin} from "vuelidate";
	import {required, numeric} from "vuelidate/lib/validators";

	export default {
		name: 'ManageTables',
		props: ['client'],
		mixins: [validationMixin],
		data: () => ({
			tables: [],
			toDelete: [],
			form: {
				label: '',
				capacity: 0,
			},
		}),
		validations: {
			form: {
				capacity: {
					required,
					numeric
				},
				label: {
					required
				}
			}
		},
		methods: {
			populate: function(){
				const req = new Restaurant();
				req.setId(this.$route.params.id);
				const promise = this.client.client.getTablesByRestaurant(req, {}, err => {
					console.log(err);
				});
				promise.on('data', (data) => {
					this.tables.push(data);
				});
			},
			getValidationClass(elem){
				const field = this.$v.form[elem];
				if(field)
					return {
						'md-invalid': field.$invalid && field.$dirty
					};
			},
			validateForm: function() {
				this.$v.$touch();
				const req = new CreateTableRequest();
				const restaurant = new Restaurant();
				restaurant.setId(this.$route.params.id);
				req.setTarget(restaurant);
				const table = new Table();
				table.setLabel(this.form.label);
				table.setCapacity(this.form.capacity);
				req.setTable(table);
				this.client.client.createTable(req, {}, err => {
					console.log(err);
				});
				this.tables.push(table);
			},
			delTab: function(toDelete) {
				const req = new Table();
				req.setId(toDelete);
				this.client.client.deleteTable(req, {}, err => {
					console.log(err);
				});
				delete this.tables[this.tables.indexOf(toDelete)];
			},
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
