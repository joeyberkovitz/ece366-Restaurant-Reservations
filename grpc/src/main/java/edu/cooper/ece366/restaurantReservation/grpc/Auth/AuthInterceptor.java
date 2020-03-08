package edu.cooper.ece366.restaurantReservation.grpc.Auth;


import io.grpc.*;
import org.jdbi.v3.core.Jdbi;

import java.util.Properties;
import java.util.Set;

public class AuthInterceptor implements ServerInterceptor {

	public static final Context.Key<String> CURRENT_USER = Context.key("currentUser");

	public AuthInterceptor(){ }

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
		Set<String> keys = headers.keys();
		if(!keys.contains("authorization")){
			throw new StatusRuntimeException(Status.UNAUTHENTICATED);
		}

		String authToken = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
		String username;
		try{
			username = AuthManager.validateToken(authToken);
		}
		catch (RuntimeException e){
			throw new StatusRuntimeException(Status.UNAUTHENTICATED);
		}
		//Pass username into context
		final Context context = Context.current().withValue(CURRENT_USER, username);
		return Contexts.interceptCall(context, call, headers, next);
		//return next.startCall(call, headers);
	}
}
