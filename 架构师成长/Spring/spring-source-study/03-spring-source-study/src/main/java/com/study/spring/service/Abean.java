package com.study.spring.service;

import java.util.Locale;

import com.study.spring.ext.MyComponetAnno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

//@Componet
//@Service
@MyComponetAnno
public class Abean {

	@Autowired
	private ApplicationContext applicationContext;

	public Abean() {
		System.out.println("-----------------Abean 被实例化了。。。。。。。。。");
	}

	public void doSomething() {
		System.out.println(this + " do something .....love="
				+ this.applicationContext.getEnvironment().getProperty("mike.love"));
		System.out
				.println("-----------mike.name=" + this.applicationContext.getMessage("mike.name", null, Locale.CHINA));
	}
}
