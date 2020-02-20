package edu.cooper.ece366.restaurantReservation.grpc;

import io.grpc.*;
//Based on https://stackoverflow.com/questions/39797142/how-to-add-global-exception-interceptor-in-grpc-server

public class StatusExceptionInterceptor implements ServerInterceptor {

	@Override
	public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
		ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
		return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {
			@Override
			public void onHalfClose() {
				try {super.onHalfClose();}
				catch (Exception e){
					if(e.getClass() == StatusRuntimeException.class){
						Metadata trailers = ((StatusRuntimeException) e).getTrailers();
						if(trailers == null){
							trailers = new Metadata();
						}
						call.close(
							((StatusRuntimeException) e).getStatus(),
							trailers
						);
					}
					else {
						e.printStackTrace();
						call.close(Status.INTERNAL
							.withDescription("Unknown error occurred"),
							new Metadata());
					}
				}
			}
		};
	}
}
