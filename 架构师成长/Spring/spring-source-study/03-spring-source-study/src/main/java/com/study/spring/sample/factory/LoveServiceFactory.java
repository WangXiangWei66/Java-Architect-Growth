package com.study.spring.sample.factory;

import com.study.spring.service.CombatService;
import com.study.spring.service.LoveService;
import com.study.spring.service.LoveServiceImpl;

public class LoveServiceFactory {

	public static LoveService getLoveServiceFromStaticFactoryMethod() {
		return new LoveServiceImpl();
	}

	public CombatService getCombatServiceFromMemberFactoryMethod(int time) {
		return new CombatService(time);
	}
}
