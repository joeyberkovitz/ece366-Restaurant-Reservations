package edu.cooper.ece366.restaurantReservation.spring;

public class FileUploadResponse {
	private final String message;

	public FileUploadResponse(String message){
		this.message = message;
	}

	public String getContent() { return message; }
}
