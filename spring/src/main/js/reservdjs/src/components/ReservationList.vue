<template>
    <md-card class="md-layout-item md-size-55 md-big-size-50 md-alignment-top-center">
        <md-card-header>
            <div class="title">
                <h1>{{ $route.name }}</h1>
                <div class="md-layout md-gutter">
                    <div class="md-layout-item">
                        <md-field>
                            <label for="status">Status</label>
                            <md-select name="status" id="status" v-model="filterData.status">
                                <md-option v-for="(status) in statuses" :key="status" :value="status">{{ status }}</md-option>
                                <md-option value="ALL">ALL</md-option>
                            </md-select>
                        </md-field>
                    </div>
                    <div class="md-layout-item">
                        <md-datepicker name="start date" id="start date" v-model="filterData.date"><label for="start date">Start Date</label></md-datepicker>
                    </div>
                    <div class="md-layout-item">
                        <md-datepicker name="end date" id="end date" v-model="filterData.futureDate"><label for="end date">End Date</label></md-datepicker>
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
</template>

<script>
    import {Reservation} from "../proto/RestaurantService_pb";
    import moment from "moment";

    export default {
        name: "ReservationList",
        props: ['reservations'],
        created() {
            this.filterData.date = new Date();
            this.filterData.date.setHours(0);
            this.filterData.date.setMinutes(0);
            this.filterData.date.setSeconds(0);
            this.filterData.date.setMilliseconds(0);
            this.filterData.futureDate = new Date(this.filterData.date.getFullYear(), this.filterData.date.getMonth(),
                this.filterData.date.getDate()+7);
            this.filterData.status = "ALL";
            this.statuses = Object.keys(Reservation.ReservationStatus);

            this.load();
        },
        data: () => ({
            filterData: {
                date: null,
                futureDate: null,
                status: null
            },
            statuses: []
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
                this.$emit('load',this.filterData);
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

</style>