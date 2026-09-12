package com.aura.aura_outfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AuraOutfitApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuraOutfitApplication.class, args);
	}

}
