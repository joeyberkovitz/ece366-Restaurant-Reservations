package edu.cooper.ece366.restaurantReservation.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class StaticResourceProvider implements WebMvcConfigurer {

	@Value("${file.uploadPath}")
	private String uploadPath;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry){
		registry.addResourceHandler("/file/**")
				.addResourceLocations(uploadPath)
				.setCachePeriod(0);
	}
}
