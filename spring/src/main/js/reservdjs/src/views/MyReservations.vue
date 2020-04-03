<template>
    <div class="centered">
        <md-card class="md-layout-item md-size-55 md-big-size-50 md-alignment-top-center">
            <md-card-header>
                <div class="title">
                    <h1>{{ $route.name }}</h1>
                    <div class="md-layout md-gutter">
                        <div class="md-layout-item">
                        <md-field>
                            <label for="status">Status</label>
                            <md-select name="status" id="status" v-model="status">
                                    <md-option v-for="(status) in statuses" :key="status" :value="status">{{ status }}</md-option>
                                    <md-option value="ALL">ALL</md-option>
                            </md-select>
                        </md-field>
                        </div>
                        <div class="md-layout-item">
                            <md-datepicker name="start date" id="start date" v-model="date"><label for="start date">Start Date</label></md-datepicker>
                        </div>
                        <div class="md-layout-item">
                            <md-datepicker name="end date" id="end date" v-model="futureDate"><label for="end date">End Date</label></md-datepicker>
                        </div>
                        <div class="md-layout-item">
                            <md-button class="bold md-primary" @click="load()">Filter</md-button>
                        </div>
                    </div>
                </div>
            </md-card-header>
            <md-card-content>
                <md-list class="md-triple-line">
                    <div v-for="(reservation) in reservations" :key="reservation.details.getId()" :value="reservation.details.getId()">
                        <md-list-item>
                            <div class="md-list-item-text">
                                <span>{{ reservation.details.getRestaurant().getName() }} - {{ getStatus(reservation.details.getStatus()) }}</span>
                                <span class="filter">{{ reservation.details.getStarttime() | momentStart(reservation.details.getRestaurant().getRtime()) }}</span>
                                <span>{{ reservation.invites | expand }}</span>
                            </div>
                            <md-button class="bold md-primary" @click="redirect()">Edit Reservation</md-button>
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
    import {Reservation, ReservationUserRequest} from "../proto/RestaurantService_pb";
    import moment from "moment";

    export default {
        name: "MyReservations",
        created() {
            this.date = new Date();
            this.date.setHours(0);
            this.date.setMinutes(0);
            this.date.setSeconds(0);
            this.date.setMilliseconds(0);
            this.futureDate = new Date(this.date.getFullYear(), this.date.getMonth(), this.date.getDate()+7);
            this.status = "ALL";
            this.statuses = Object.keys(Reservation.ReservationStatus);

            this.load();
        },
        data: () => ({
            date: null,
            futureDate: null,
            status: null,
            statuses: [],
            reservations: []
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
            load() {
                this.reservations = [];

                const client = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
                const request = new ReservationUserRequest();
                request.setBegintime(this.date.getTime()/1000);
                request.setFuturetime(this.futureDate.getTime()/1000);
                request.setStatus(this.status);
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
            },
            getStatus(val) {
               for (let i = 0; i < this.statuses.length; i++){
                   if (Reservation.ReservationStatus[this.statuses[i]] === val){
                       return this.statuses[i];
                   }
               }
               return "";
            },
            redirect() {
                return null;
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