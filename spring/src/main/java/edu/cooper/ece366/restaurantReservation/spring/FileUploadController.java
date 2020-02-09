package edu.cooper.ece366.restaurantReservation.spring;

import edu.cooper.ece366.restaurantReservation.spring.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.FileAlreadyExistsException;

@RestController
public class FileUploadController {

	private final StorageService storageService;

	@Autowired
	public FileUploadController(StorageService storageService){
		this.storageService = storageService;
	}

	@PostMapping("/")
	public FileUploadResponse handleFileUpload(@RequestParam("file") MultipartFile file
		, RedirectAttributes redirectAttributes){
		storageService.store(file);
		redirectAttributes.addFlashAttribute("message", "File uploaded: " + file.getOriginalFilename());
		return new FileUploadResponse("success");
	}
}
