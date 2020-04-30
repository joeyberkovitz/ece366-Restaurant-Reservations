<template>
	<div class="centered">
		<RestaurantForm :client="this.client"
		                :categories="this.categories"
						:create="true"
		                button="Create Restaurant"
		                canEdit="true"/>
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
		this.client = this.$store.getters.grpc.restaurantClient;
		const promise = this.client.client.getCategoryList(new GetCategoryRequest(), {}, err => {
			if(err) {
				console.log(err);
				this.snackBarMessage = err.message;
				this.showSnackBar = true;
			}
		});
		promise.on('data', (data) => {
			this.categories.push(data);
		});
	},
}
</script>

<style scoped>

</style>
