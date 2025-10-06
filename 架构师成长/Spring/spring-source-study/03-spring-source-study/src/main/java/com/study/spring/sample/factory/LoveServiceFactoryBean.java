package com.study.spring.sample.factory;

import org.springframework.beans.factory.FactoryBean;

import com.study.spring.service.LoveService;
import com.study.spring.service.LoveServiceImpl;

public class LoveServiceFactoryBean implements FactoryBean<LoveService> {

	@Override
	public LoveService getObject() throws Exception {
		return new LoveServiceImpl();
	}

	@Override
	public Class<?> getObjectType() {
		return LoveService.class;
	}

}
