<template>
    <div class="centered">
        <RestaurantForm :client="this.client"
                        :categories="this.categories"
                        :create="false"
                        button="Null"
                        :canEdit="false"/>
    </div>
</template>

<script>
    import RestaurantForm from '@/components/RestaurantForm.vue';
    import {GetCategoryRequest} from "../proto/RestaurantService_pb";

    export default {
        name: "ViewRestaurant",
        components: {
            RestaurantForm
        },
        data: () => ({
            client: null,
            categories: [],
        }),
        created() {
            this.client = this.$store.getters.grpc.restaurantClient;
            const promise = this.client.getCategoryList(new GetCategoryRequest(), {}, err => {
                if(err) {
                    console.log(err);
                    this.snackBarMessage = err.message;
                    this.showSnackBar = true;
                }
            });
            promise.on('data', (data) => {
                this.categories.push(data);
            });

            this.$root.$on('restaurant', (event) => {
                console.log(event);
                const id = event;
                if(id != this.$route.params.id) {
                    this.$router.push({params: {id:id}})
                }
            });
        },
    }
</script>

<style scoped>

</style>