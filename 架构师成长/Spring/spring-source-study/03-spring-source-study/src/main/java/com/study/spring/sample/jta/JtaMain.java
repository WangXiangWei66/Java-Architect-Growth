package com.study.spring.sample.jta;

import com.study.spring.sample.jta.entity.User;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.study.spring.sample.jta.service.UserService;

@Configuration
@ComponentScan("com.study.spring.sample.jta")
@ImportResource("classpath:com/study/spring/sample/jta/application.xml")
@EnableTransactionManagement
public class JtaMain {

	public static void main(String[] args) {
		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JtaMain.class);) {
			User user = new User();
			user.setId("1234569");
			user.setUserName("mike-666666666");

			UserService userService = context.getBean(UserService.class);
			userService.insertUser(user);
		}
	}
}
