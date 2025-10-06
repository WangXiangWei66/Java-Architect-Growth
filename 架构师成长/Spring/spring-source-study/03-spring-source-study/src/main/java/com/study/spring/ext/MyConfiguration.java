package com.study.spring.ext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
@PropertySource("classpath:/application.properties")
public class MyConfiguration {

	@Bean("messageSource")
	public ReloadableResourceBundleMessageSource getReloadableResourceBundleMessageSource() {
		ReloadableResourceBundleMessageSource rms = new ReloadableResourceBundleMessageSource();
		rms.setBasename("message");
		return rms;
	}
}
