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
	</div>
</template>

<script>
import RestaurantForm from '@/components/RestaurantForm.vue'
import ManageUsers from '@/components/ManageUsers.vue'
import ManageTables from '@/components/ManageTables.vue'
import {GetCategoryRequest, User} from "../proto/RestaurantService_pb";

export default {
	name: "SetRestaurant",
	components: {
		RestaurantForm,
		ManageUsers,
		ManageTables,
	},
	data: () => ({
		client: null,
		categories: [],
		restaurants: [],
		users: [],
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
}
</script>

<style scoped>

</style>
