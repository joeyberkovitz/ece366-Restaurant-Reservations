package edu.cooper.ece366.restaurantReservation.grpc.Auth;


import io.grpc.*;

import java.util.Set;

public class AuthInterceptor implements ServerInterceptor {

	private AuthManager authManager;

	public AuthInterceptor(AuthManager authManager){
		this.authManager = authManager;
	}

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
		Set<String> keys = headers.keys();
		if(!keys.contains("Authorization")){
			throw new StatusRuntimeException(Status.UNAUTHENTICATED);
		}

		String authToken = headers.get(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER));
		try{
			authManager.validateToken(authToken);
		}
		catch (RuntimeException e){
			throw new StatusRuntimeException(Status.UNAUTHENTICATED);
		}
		return next.startCall(call, headers);
	}
}
