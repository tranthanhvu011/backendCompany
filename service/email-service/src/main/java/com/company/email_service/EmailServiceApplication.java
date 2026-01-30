package com.company.email_service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class EmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailServiceApplication.class, args);
	}

	/**
	 * Test gửi email khi khởi động (Comment lại sau khi test xong)
	 */
	@Bean
	CommandLineRunner testEmail(EmailSenderService emailSenderService) {
		return args -> {
			System.out.println("====================================");
			System.out.println("Testing email service...");
			
			try {
				emailSenderService.sendSimpleEmail(
					"vutranorhilsun@gmail.com",  // Gửi cho chính mình để test
					"Test Email từ Email-Service",
					"Đây là email test từ microservice.\nNếu bạn nhận được email này, nghĩa là Email-Service hoạt động OK!"
				);
				System.out.println("✅ Email sent successfully!");
			} catch (Exception e) {
				System.out.println("❌ Email failed: " + e.getMessage());
				e.printStackTrace();
			}
			
			System.out.println("====================================");
		};
	}
}
