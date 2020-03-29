<template>
    <div class="centered">
        <md-card class="md-layout-item md-size-50 md-big-size-50 md-alignment-top-center">
            <md-card-header>
                <div class="title">
                    <h1>User Profile</h1>
                    <md-button type="button" class="button md-dense md-raised md-primary" @click="type = 'Edit'"
                               v-if="type !==	'Edit'">EDIT</md-button>
                </div>
            </md-card-header>
            <md-card-content>
                <form novalidate class="md-layout md-alignment-center-center" @submit.prevent="validateForm">
                    <md-field>
                        <label for="username">Username</label>
                        <md-input name="username" id="username" v-model="profile.username" readonly></md-input>
                    </md-field>
                    <md-field :class="getValidationClass('firstName')">
                        <label for="firstName">First Name</label>
                        <md-input name="firstName" id="firstName" v-model="profile.firstName"
                                  :readonly="type==='View'"></md-input>
                        <span class="md-error" v-if="!$v.profile.firstName.required">First Name is required</span>
                    </md-field>
                    <md-field :class="getValidationClass('lastName')">
                        <label for="lastName">Last Name</label>
                        <md-input name="lastName" id="lastName" v-model="profile.lastName"
                                  :readonly="type==='View'"></md-input>
                        <span class="md-error" v-if="!$v.profile.lastName.required">Last Name is required</span>
                    </md-field>
                    <md-field :class="getValidationClass('email')">
                        <label for="email">Email</label>
                        <md-input name="email" id="email" v-model="profile.email" :readonly="type==='View'"></md-input>
                        <span class="md-error" v-if="!$v.profile.email.required">Email is required</span>
                        <span class="md-error" v-if="!$v.profile.email.email">Invalid email address</span>
                    </md-field>
                    <md-field :class="getValidationClass('phone')">
                        <label for="phone">Phone</label>
                        <md-input name="phone" id="phone" v-model="profile.phone" :readonly="type==='View'"></md-input>
                        <span class="md-error" v-if="!$v.profile.phone.required">Phone is required</span>
                        <span class="md-error" v-if="!$v.profile.phone.numeric">Phone number may only
									consist of numbers</span>
                    </md-field>
                    <md-button type="button" class="button md-dense md-raised md-primary" @click="cancel()"
                               :disabled="sending" v-if="type !==	'View'">Cancel</md-button>
                    <md-button type="submit" class="button md-dense md-raised md-primary" :disabled="sending"
                               v-if="type !== 'View'">Submit</md-button>
                </form>
            </md-card-content>
        </md-card>
    </div>
</template>

<script>
    import {validationMixin} from "vuelidate";
    import {email, numeric, required} from "vuelidate/lib/validators";
    import {CustomRPCClient} from "../proto/CustomRPCClient";
    import {UserServiceClient} from "../proto/RestaurantServiceServiceClientPb";
    import {Contact, User} from "../proto/RestaurantService_pb";

    export default {
        name: "ManageUser.vue",
        mixins: [validationMixin],
        data: () => ({
            type: "View",
            profile: {
                username: null,
                firstName: null,
                lastName: null,
                email: null,
                phone: null,
            },
            initialProfile: {
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
        validations(){
            const ret = {
                profile: {
                    username: {
                        required
                    },
                    firstName: {
                        required
                    },
                    lastName:{
                        required
                    },
                    phone: {
                        required,
                        numeric
                    },
                    email: {
                        required,
                        email
                    }
                }
            };
            return ret;
        },
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

                    this.initialProfile = JSON.parse(JSON.stringify(this.profile));
                }
                else{
                    console.log(err);
                    this.snackBarMessage = err.message;
                    this.showSnackBar = true;
                }
            });
        },
        methods: {
            getValidationClass(elem){
                const field = this.$v.profile[elem];
                if(field)
                    return {
                        'md-invalid': field.$invalid && field.$dirty
                    };
            },
            validateForm () {
                this.$v.$touch();

                if(!this.$v.$invalid) {
                    this.sending = true;
                    this.submit();
                }
                else {
                    this.snackBarMessage = "Invalid form";
                    this.showSnackBar = true;
                }
            },
            cancel() {
                this.type = 'View';
                this.profile = JSON.parse(JSON.stringify(this.initialProfile));
            },
            submit() {
                const client = new CustomRPCClient(UserServiceClient, this.$store.getters.config.host);

                const user = new User();
                const contact = new Contact();
                user.setId(2);
                user.setUsername(this.profile.username);
                user.setFname(this.profile.firstName);
                user.setLname(this.profile.lastName);
                contact.setEmail(this.profile.email);
                contact.setPhone(this.profile.phone);
                user.setContact(contact);

                client.client.setUser(user,{},(err, response) => {
                    if (!err){
                        console.log(response);

                        this.initialProfile = JSON.parse(JSON.stringify(this.profile));
                        this.type = 'View';
                        this.sending = false;
                    }
                    else{
                        console.log(err);
                        this.snackBarMessage = err.message;
                        this.showSnackBar = true;
                    }
                });
            }
        }
    }
</script>

<style scoped lang="scss">
    .centered {
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;

    }

    .title {
        text-align: left;
    }
</style>