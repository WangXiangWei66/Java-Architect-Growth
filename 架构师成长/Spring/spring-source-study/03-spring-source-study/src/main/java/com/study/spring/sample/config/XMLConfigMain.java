package com.study.spring.sample.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class XMLConfigMain {

	public static void main(String[] args) {
		ApplicationContext context = new GenericXmlApplicationContext(
				"classpath:com/study/spring/sample/config/application.xml");

		BeanF bf = context.getBean(BeanF.class);
		bf.do1();

	}

}
