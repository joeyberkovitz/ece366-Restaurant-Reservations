<template>
	<div class="centered">
		<RestaurantForm :client="this.client" :categories="this.categories"/>
	</div>
</template>

<script>
import RestaurantForm from '@/components/RestaurantForm.vue'
import {GetCategoryRequest} from "../proto/RestaurantService_pb";

export default {
	name: "NewRestaurant",
	components: {
		RestaurantForm
	},
	data: () => ({
		client: null,
		categories: [],
	}),
	created() {
		const client = this.$store.getters.grpc.restaurantClient;
		const promise = client.client.getCategoryList(new GetCategoryRequest(), {}, err => {
			console.log(err);
		});
		promise.on('data', (data) => {
			this.categories.push(data);
		});
	},
}
</script>

<style scoped>

</style>
