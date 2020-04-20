<template>
    <div class="centered">
        <ReservationList :reservations="this.reservations"
                         @load="load(arguments)"/>

    </div>
</template>

<script>
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {ReservationServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import ReservationUserRequest from "../proto/RestaurantService_pb";
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
            this.client = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
        },
        methods: {
            load(args) {
                this.reservations = [];

                const request = new ReservationUserRequest();
                request.setBegintime(args[0].getTime()/1000);
                request.setFuturetime(args[1].getTime()/1000);
                request.setStatus(args[2]);
                const promise1 = this.client.client.getReservationsByUser(request,{}, err => {
                    console.log(err);
                });
                promise1.on('data', (data1) => {
                    const users = [];
                    const promise2 = this.client.client.listReservationUsers(data1, {}, err => {
                        console.log(err);
                    });
                    promise2.on('data', (data2) => {
                        users.push(data2.getFname() + " " + data2.getLname());
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