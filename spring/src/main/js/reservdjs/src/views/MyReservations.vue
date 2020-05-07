<template>
    <div class="centered">
        <ReservationList :reservations="this.reservations"
                         @load="load($event)"
                         :restView="false"/>

    </div>
</template>

<script>
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {ReservationServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import {ReservationUserRequest} from "../proto/RestaurantService_pb";
    import ReservationList from "../components/ReservationList";

    export default {
        name: "MyReservations",
        components: {ReservationList},
        data: () => ({
            client: null,
            date: null,
            futureDate: null,
            status: null,
            reservations: []
        }),
        created() {
            this.client = this.$store.getters.grpc.reservationClient;
        },
        methods: {
            load(filterData) {
                this.reservations = [];

                const request = new ReservationUserRequest();
                request.setBegintime(filterData.date.getTime()/1000);
                request.setFuturetime(filterData.futureDate.getTime()/1000);
                request.setStatus(filterData.status);
                const promise1 = this.client.getReservationsByUser(request,{}, err => {
                    if(err) {
                        console.log(err);
                        this.snackBarMessage = err.message;
                        this.showSnackBar = true;
                    }
                });
                promise1.on('data', (data1) => {
                    const users = [];
                    const promise2 = this.client.listReservationUsers(data1, {}, err => {
                        if(err) {
                            console.log(err);
                            this.snackBarMessage = err.message;
                            this.showSnackBar = true;
                        }
                    });
                    promise2.on('data', (data2) => {
                        users.push(data2);
                    });
                    this.reservations.push({details: data1,
                        invites: users});
                });
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

    .title {
        text-align: left;
    }

    .bold {
        font-weight: bold;
    }
</style>
