<template>
	<div class="centered">
		<md-card class="md-layout md-alignment-center-center">
		        <label for="restaurant">Restaurant</label>
		        <select name="restaurant" id="restaurantSel"
		                v-on:change="$root.$emit('restaurant', $event)">
		                <option selected key="0" value="0">---</option>
			        <option v-for="(restaurant) in restaurants"
			                :key="restaurant.getId()"
        			        :value="restaurant.getId()">{{restaurant.getName()}}</option>
	        	</select>
		</md-card>
		<RestaurantForm :client="this.client"
		                :categories="this.categories"
		                button="Set Restaurant"/>
	</div>
</template>

<script>
import RestaurantForm from '@/components/RestaurantForm.vue'
import {GetCategoryRequest, User} from "../proto/RestaurantService_pb";

export default {
	name: "SetRestaurant",
	components: {
		RestaurantForm
	},
	data: () => ({
		client: null,
		categories: [],
		restaurants: [],
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
