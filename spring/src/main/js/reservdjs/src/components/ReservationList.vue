<template>
  <div class="md-layout md-alignment-center-center">
    <md-card class="md-layout-item md-size-50 md-big-size-50">
        <md-card-header>
            <div class="title">
                <h1>Reservations</h1>
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
                            <div class="user-entry"><span v-for="user in reservation.invites" :key="user.getId()"><span @click="invite(user.getId(), reservation, 0)" v-if="curUser != user.getId() && getStatus(reservation.details.getStatus()) == 'OPENED'">&#9940;</span>{{ user.getFname() }} {{ user.getLname() }}</span></div>
                            <form @submit.prevent="invite($event.target[0].value, reservation, 1)"
                                  v-if="getStatus(reservation.details.getStatus()) == 'OPENED'">
                            	<md-field>
                            	    <label :for="'invite-'+reservation.details.getId()">Invite user</label>
                            	    <md-input :id="'invite-'+reservation.details.getId()"/>
                            	</md-field>
                            	<md-button type="submit" class="button md-primary">Go</md-button>
                            </form>
                            <div v-if="showTables">
                            	<div v-for="table in reservation.tables" :key="table.getLabel()">Table(s): {{ table.getLabel() }} ({{ table.getCapacity() }})</div>
                            </div>
                        </div>
                        <md-button v-if="getStatus(reservation.details.getStatus()) == 'OPENED'"
                                   class="bold md-primary"
                                   @click="cancel(reservation.details);">
                                   Cancel Reservation</md-button>
                    </md-list-item>
                    <md-divider></md-divider>
                </div>
            </md-list>
        </md-card-content>
    </md-card>
  </div>
</template>

<script>
    import {Reservation, User, InviteMessage} from "../proto/RestaurantService_pb";
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {ReservationServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import moment from "moment";

    export default {
        name: "ReservationList",
        props: ['reservations', 'showTables'],
        created() {
            this.curUser = JSON.parse(atob(this.$store.getters.user.authToken.split('.')[1])).sub;
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
            statuses: [],
            curUser: 0,
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
            cancel(reservation) {
		const resClient = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
		reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
		resClient.client.setReservation(reservation, {}, err => {
		    console.log(err);
		    this.load();
		});
            },
            invite(username, reservationOrig, inviteBool) {
		const resClient = new CustomRPCClient(ReservationServiceClient, this.$store.getters.config.host);
		const request = new InviteMessage();
		const reservation = new Reservation();
		reservation.setId(reservationOrig.details.getId());
		request.setReservation(reservation);
		const user = new User();
		if(inviteBool)
			user.setUsername(username);
		else
			user.setId(username);
		request.setUser(user);
		const callback = err => {
                    reservationOrig.invites = [];
                    const promise2 = resClient.client.listReservationUsers(reservation, {}, err => {
                        console.log(err);
                    });
                    promise2.on('data', (data) => {
                        reservationOrig.invites.push(data);
                    });
		}
		if(inviteBool)
			resClient.client.inviteReservation(request, {}, callback);
		else
			resClient.client.removeReservationUser(request, {}, callback);
            },
        }
    }
</script>

<style scoped>
.user-entry > span:not(:last-child)::after {
	content: ', ';
}
.user-entry > span > span {
	cursor: pointer;
}
</style>
