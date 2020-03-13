import * as client from './RestaurantService_grpc_web_pb.js';
import * as msgs from './RestaurantService_pb.js';
const host = 'http://'+window.location.hostname+':8080';
console.log("test");

document.getElementById('create_user').addEventListener('submit',
	event => {
		var authClient = new client.AuthServiceClient(host,null,null);
		var req = new client.CreateUserRequest();
		var user = new client.User();
		user.setUsername(event.target.elements['user'].value);
		user.setFname(event.target.elements['fname'].value);
		user.setLname(event.target.elements['lname'].value);
		var contact = new client.Contact();
		contact.setPhone(event.target.elements['phone'].value);
		contact.setEmail(event.target.elements['email'].value);
		user.setContact(contact);
		req.setUser(user);
		req.setPassword(event.target.elements['password'].value);
		new client.AuthServiceClient(host,null,null).createUser(req, {},
			(err, resp) => {
				console.log(err);
				console.log(resp);
			}
		);
		event.preventDefault(); // Prevent page from reloading on submit
	}
);
