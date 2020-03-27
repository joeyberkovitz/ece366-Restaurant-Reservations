<template>
    <div class="centered">
        <md-button @click="test()">TEST</md-button>
        <md-field>
            <label for="username">Username</label>
            <div id="username">{{profile.username}}</div>
        </md-field>
        <md-field>
            <label for="firstName">First Name</label>
            <div id="firstName">{{profile.firstName}}</div>
        </md-field>
        <md-field>
            <label for="lastName">Last Name</label>
            <div id="lastName">{{profile.lastName}}</div>
        </md-field>
        <md-field>
            <label for="email">Email</label>
            <div id="email">{{profile.email}}</div>
        </md-field>
        <md-field>
            <label for="phone">Phone</label>
            <div id="phone">{{profile.phone}}</div>
        </md-field>
    </div>
</template>

<script>
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {UserServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import {User} from "../proto/RestaurantService_pb";

    export default {
        name: "ManageUser.vue",
        data: () => ({
            type: "UserProfile",
            profile: {
                username: null,
                firstName: null,
                lastName: null,
                email: null,
                phone: null,
            },
            showSnackBar: false,
            snackBarMessage: "",
            snackBarDuration: 4000,
            sending: false
        }),
        created() {
            const client = new CustomRPCClient(UserServiceClient, this.$store.getters.config.host);
            //const client = this.$store.getters.grpc.restaurantClient;
            const user = new User();
            user.setId(2);
            client.client.getUser(user, {}, (err, response) => {
                if (!err){
                    console.log(response);
                    this.profile.username = response.getUsername();
                    this.profile.firstName = response.getFname();
                    this.profile.lastName = response.getLname();
                    this.profile.email = response.getContact().getEmail();
                    this.profile.phone = response.getContact().getPhone();
                }
                else{
                    console.log(err);
                    this.snackBarMessage = err.message;
                    this.showSnackBar = true;
                }
            });
        },
        methods: {
            test(){
                console.log("test");
            }
        }
    }
</script>

<style scoped lang="scss">
    centered{
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        height: 80vh;
    }

</style>