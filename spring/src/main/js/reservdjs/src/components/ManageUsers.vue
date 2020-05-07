<template>
	<div class="md-layout md-alignment-center-center">
		<md-card class="md-layout-item md-size-50 md-small-size-50">
			<md-card-header>
				<div class="md-title">
					Manage Users
				</div>
			</md-card-header>
			<md-card-content>
				<md-field>
				        <select multiple name="relationship" id="relationSel"
					        v-on:keyup.delete="delRel(toDelete)"
					        v-model="toDelete">
					        <option v-for="(user) in users"
				        	        :key="user.getId()"
	        			        	:value="user.getId()">{{user.getUsername()}}</option>
			        	</select>
		        	</md-field>
				<md-button v-on:click="delRel(toDelete)" class="button md-primary md-raised">Delete
					</md-button>
				<md-field>
					<label for="add">Username to add</label>
					<md-input name="add" id="add" type="text" v-model="toAdd"/>
				</md-field>
				<md-button v-on:click="addRel(toAdd)" class="button md-primary md-raised">Add
					</md-button>
				<md-snackbar md-position="center" :md-duration="snackBarDuration"
							 :md-active.sync="showSnackBar" md-persistent>
					<span>{{snackBarMessage}}</span>
				</md-snackbar>
			</md-card-content>
		</md-card>

	</div>
</template>

<script>
	import {Restaurant, Relationship, User} from "../proto/RestaurantService_pb";

	export default {
		name: 'ManageUsers',
		props: ['client'],
		data: () => ({
			users: [],
			toDelete: [],
			toAdd: '',
			showSnackBar: false,
			snackBarMessage: "",
			snackBarDuration: 4000
		}),
		methods: {
			populate: function(){
				this.users = [];
				const req = new Restaurant();
				req.setId(this.$route.params.id);
				const promise = this.client.getUsersByRestaurant(req, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
				});
				promise.on('data', (data) => {
					this.users.push(data.getUser())
				});
			},
			delRel: function(toDelete) {
				console.log(toDelete);
				const req = new Relationship();
				const restaurant = new Restaurant();
				restaurant.setId(this.$route.params.id);
				req.setRestaurant(restaurant);
				const user = new User();
				user.setId(toDelete);
				req.setUser(user);
				this.client.deleteRelationship(req, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
					else {
						delete this.users[this.users.indexOf(toDelete)];
						this.populate();
					}
				});
			},
			addRel: function(toAdd) {
				const req = new Relationship();
				const restaurant = new Restaurant();
				restaurant.setId(this.$route.params.id);
				req.setRestaurant(restaurant);
				const user = new User();
				user.setUsername(toAdd);
				req.setUser(user);
				req.setRole(Relationship.UserRole.MANAGER);
				this.client.addRelationship(req, {}, err => {
					if(err) {
						console.log(err);
						this.snackBarMessage = err.message;
						this.showSnackBar = true;
					}
					else {
						this.users.push(user);
						this.populate();
					}
				});
			}
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
