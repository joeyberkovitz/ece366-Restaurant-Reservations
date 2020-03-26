<template>
	<div>
		<md-button @click="test()">TEST</md-button>
	</div>
</template>

<script>
	import {Restaurant} from "../proto/RestaurantService_pb";
	import {CustomRPCClient} from "../proto/CustomRPCClient";
	import {RestaurantServiceClient} from "../proto/RestaurantServiceServiceClientPb";

	export default {
		name: "NewRestaurant",
		methods: {
			test(){
				const client = new CustomRPCClient(RestaurantServiceClient, this.$store.getters.config.host);
				//const client = this.$store.getters.grpc.restaurantClient;
				const restaurant = new Restaurant();
				restaurant.setId(1);
				client.client.getRestaurant(restaurant, {}, (err, response) => {
					console.log(err, response);
				});
			}
		}
	}
</script>

<style scoped>

</style>