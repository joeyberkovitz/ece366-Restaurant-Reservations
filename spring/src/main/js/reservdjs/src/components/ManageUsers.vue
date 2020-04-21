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
				        	        :key="user.getUser().getId()"
	        			        	:value="user.getUser().getId()">{{user.getUser().getUsername()}}</option>
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
		}),
		methods: {
			populate: function(){
				const req = new Restaurant();
				req.setId(this.$route.params.id);
				const promise = this.client.client.getUsersByRestaurant(req, {}, err => {
					console.log(err);
				});
				promise.on('data', (data) => {
					this.users.push(data);
				});
			},
			delRel: function(toDelete) {
				const req = new Relationship();
				req.setId(toDelete);
				this.client.client.deleteRelationship(req, {}, err => {
					console.log(err);
				});
				delete this.users[this.users.indexOf(toDelete)];
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
				this.client.client.setRelationship(req, {}, err => {
					console.log(err);
				});
				this.users.push(user);
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
