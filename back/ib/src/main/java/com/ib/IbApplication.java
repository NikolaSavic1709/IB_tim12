package com.ib;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class IbApplication{
	public static void main(String[] args) {
		SpringApplication.run(IbApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate(){
		return new RestTemplateBuilder().build();
	}
//	@Override
//	public void run(String... args) throws Exception {
//		String sql = "INSERT INTO students (name, email) VALUES ("
//				+ "'Nam Ha Minh', 'nam@codejava.net')";
//		int rows = jdbcTemplate.update(sql);
//		if (rows > 0) {
//			System.out.println("A new row has been inserted.");
//		}
//	}
}

