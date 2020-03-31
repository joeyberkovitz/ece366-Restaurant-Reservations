<template>
    <div class="centered">
        <md-card class="md-layout-item md-size-50 md-big-size-50 md-alignment-top-center">
            <md-card-header>
                <div class="title">
                    <h1>{{ $route.name }}</h1>
                    <md-button class="bold md-primary" @click="test()">Test</md-button>
                    <div class="md-layout">
                        <md-menu md-direction="bottom-start" class="md-layout-item">
                            <md-button md-menu-trigger>Bottom Start</md-button>
                            <md-menu-content>
                                <md-menu-item>My Item 1</md-menu-item>
                                <md-menu-item>My Item 2</md-menu-item>
                                <md-menu-item>My Item 3</md-menu-item>
                            </md-menu-content>
                        </md-menu>
                        <md-datepicker class="md-layout-item" v-model="date" />
                        <md-datepicker class="md-layout-item" v-model="futureDate" />
                        <md-button class="bold md-primary" @click="filter()">Filter</md-button>
                    </div>
                </div>
            </md-card-header>
            <md-card-content>
                <md-list class="md-triple-line">
                    <div v-for="(reservation) in filteredReservations" :key="reservation.details.getId()" :value="reservation.details.getId()">
                        <md-list-item>
                            <div class="md-list-item-text">
                                <span>{{ reservation.details.getRestaurant().getName() }}</span>
                                <span class="filter">{{ reservation.details.getStarttime() | momentStart(reservation.details.getRestaurant().getRtime()) }}</span>
                                <span>{{ reservation.invites | expand }}</span>
                            </div>

                            <md-button class="bold md-primary">Edit Reservation</md-button>

                        </md-list-item>
                        <md-divider></md-divider>
                    </div>
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
            this.date = new Date();
            this.futureDate = new Date(this.date.getFullYear(), this.date.getMonth(), this.date.getDate()+7);

            const client = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
            const request = new ReservationUserRequest();
            const promise1 = client.client.getReservationsByUser(request,{}, err => {
                console.log(err);
            });
            promise1.on('data', (data1) => {
                const users = [];
                const promise2 = client.client.listReservationUsers(data1, {}, err => {
                    console.log(err);
                });
                promise2.on('data', (data2) => {
                    users.push(data2.getFname() + " " + data2.getLname());
                });
                this.reservations.push({details: data1,
                                        invites: users});
            });
            this.filter();
        },
        data: () => ({
            date: null,
            futureDate: null,
            reservations: [],
            filteredReservations: []
        }),
        filters: {
            momentStart: function (date, Rtime) {
                const startTime = moment.unix(date);
                const endTime = startTime.clone();
                endTime.add(Rtime, 'h');

                const startTimeString = startTime.format('MMMM Do YYYY, h:mm a');
                let endTimeString;
                if (startTime.get('d') !== endTime.get('d'))
                    endTimeString = endTime.format('MMMM Do, h:mm a');
                else
                    endTimeString = endTime.format('h:mm a');
                return startTimeString + "-" + endTimeString;
            },
            expand: function (invites) {
                let invitesString = "";
                for (let i = 0; i < invites.length; i++) {
                    invitesString += invites[i];
                    if (i !== invites.length - 1)
                        invitesString += ", ";
                }
                return invitesString;
            }
        },
        methods: {
            test() {
                console.log(this.date);
                console.log(this.futureDate);
                console.log(this.reservations);
            },
            filter() {
                const date = this.date;
                const futureDate = this.futureDate;
                this.filteredReservations = this.reservations.filter(function (a) {
                    const resDate = new Date(a.details.getStarttime()*1000);
                    return (resDate >= date && resDate <= futureDate);
                });
                this.filteredReservations.sort(function (a,b){return a.details.getStarttime()-b.details.getStarttime()});
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