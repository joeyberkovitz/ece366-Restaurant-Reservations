import store, {UserState} from '@/store/index';
import router from '@/router/index';
import {RefreshRequest} from "@/proto/RestaurantService_pb";

//Based on example here: https://github.com/grpc/grpc-web/issues/351
export class CustomRPCClient<T> {
	public readonly client: T;

	// client takes 3 arguments to instantiate
	constructor(client: { new (arg1: string, arg2: any, arg3: any): T }, arg1: string) {
		const c = new client(arg1, null, null);
		//console.log(c.client_.rpcCall);
		this.client = this.mapMetadata(c);
	}

	// Intercepts all requests to methods on the grpc client and
	// adds sitewide headers to the second argument.
	//
	// This way, you don't need to pass those headers in for every single request. :)
	//
	//Todo: make sure this works with streaming call
	//Todo: make cleaner
	//Todo: figure out why gRPC response comes through twice
	private mapMetadata<T>(client: T): T {
		for (const prop in client) {
			if (typeof client[prop] !== 'function') {
				continue
			}

			console.log(prop);

			const original = (client[prop] as unknown) as Function;
			client[prop] = ((...args: any[]) => {
				console.log("Starting function: " + prop);
				const originalCB = args[2];
				args[1] = {
					...args[1],
					Authorization: store.getters.user.authToken,
				};
				args[2] = (err, resp) => {
					console.log(err, resp);
					if(err != null && err.code == 16){
						console.log(err, resp);
						console.log("AUTH ERROR");
						const authClient = store.getters.grpc.authClient;
						const refreshRequest = new RefreshRequest();
						refreshRequest.setRefreshtoken(store.getters.user.refreshToken);
						refreshRequest.setUseragent(navigator.userAgent);
						authClient.refreshToken(refreshRequest, {}, (err, resp) => {
							if(err){
								store.commit('storeUserState', null);
								router.push("/login");
							}
							else{
								const userState = new UserState();
								userState.refreshToken = resp.getRefreshtoken();
								userState.authToken = resp.getAuthtoken();
								store.commit('storeUserState', userState);
								args[1]['Authorization'] = resp.getAuthtoken();
								original.call(client, ...args);
							}
						});
					}
					else{
						originalCB(err, resp);
					}
				};

				return original.call(client, ...args)
			}) as any
		}
		return client
	}
}