package com.study.spring.sample.di;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class DiMain {

	public static void main(String[] args) {
		ApplicationContext context = new GenericXmlApplicationContext(
				"classpath:com/study/spring/sample/di/application.xml");

		// ApplicationContext context = new
		// AnnotationConfigApplicationContext("com.study.spring.sample.di");

		Bean2 b2 = context.getBean(Bean2.class);
		b2.do2();
	}
}
