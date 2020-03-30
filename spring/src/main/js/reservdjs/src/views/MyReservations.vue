<template>
    <div class="centered">
        <md-card class="md-layout-item md-size-50 md-big-size-50 md-alignment-top-center">
            <md-card-header>
                <div class="title">
                    <h1>{{ $route.name }}</h1>
                    <md-button type="button" class="button md-dense md-raised md-primary" @click="test()">EDIT</md-button>
                </div>
            </md-card-header>
            <md-card-content>
                <md-list class="md-triple-line">
                    <md-list-item v-for="(reservation) in reservations" :key="reservation.getId()" :value="reservation.getId()">
                        <div class="md-list-item-text">
                            <span>{{ reservation.getRestaurant().getName() }}</span>
                            <span class="filter">{{ reservation.getStarttime() | moment }}</span>
                            <span>{{ reservation.getId() }}</span>
                        </div>
                    </md-list-item>
                </md-list>
            </md-card-content>
        </md-card>
    </div>
</template>

<script>
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {ReservationServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import {ReservationUserRequest} from "../proto/RestaurantService_pb";
    import moment from "moment";

    export default {
        name: "MyReservations",
        created() {
            this.d = new Date(0);
            const client = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
            const request = new ReservationUserRequest();
            const promise = client.client.getReservationsByUser(request,{}, err => {
                console.log(err);
            });
            promise.on('data', (data) => {
                this.reservations.push(data)
            });
        },
        data: () => ({
            d: null,
            reservations: []
        }),
        filters: {
            moment: function (date) {
                return moment.unix(date).format('MMMM Do YYYY, h:mm:ss a');
            }
        },
        methods: {
            test() {
                console.log(this.reservations);
            }
        }
    }
</script>

<style scoped>
    .centered {
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
    }
</style>