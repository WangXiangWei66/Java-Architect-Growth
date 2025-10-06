package com.study.spring.sample.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

public class AspectAdviceBean {

	public void before1() {
		System.out.println("----------- AspectAdviceBean before1 增强 ");
	}

	public void before2(JoinPoint jp) {
		System.out.println("----------- AspectAdviceBean before2 增强 for " + jp);
	}

	public void before3(String tk) {
		System.out.println("----------- AspectAdviceBean before3  增强  参数tk= " + tk);
	}

	public void before4(String tk, int ti) {
		System.out.println("----------- AspectAdviceBean before4  增强  参数tk= " + tk + " ti=" + ti);
	}

	public Object arround1(ProceedingJoinPoint pjp) throws Throwable {
		System.out.println("----------- AspectAdviceBean arround1 环绕-前增强 for " + pjp);
		Object ret = pjp.proceed();
		System.out.println("----------- AspectAdviceBean arround1 环绕-后增强 for " + pjp);
		return ret;
	}

	public Object arround2(ProceedingJoinPoint pjp, String name) throws Throwable {
		System.out.println("--------- AspectAdviceBean arround2 参数 name=" + name);
		System.out.println("----------- AspectAdviceBean arround2 环绕-前增强 for " + pjp);
		Object ret = pjp.proceed();
		System.out.println("----------- AspectAdviceBean arround2 环绕-后增强 for " + pjp);
		return ret;
	}

	public void afterReturning(Object retValue) {
		System.out.println("----------- AspectAdviceBean afterReturning 增强 , 返回值为： " + retValue);
	}

	public void afterThrowing(JoinPoint jp, Exception e) {
		System.out.println("----------- AspectAdviceBean afterThrowing 增强  for " + jp);
		System.out.println("----------- AspectAdviceBean afterThrowing 增强  异常 ：" + e);
	}

	public void after(JoinPoint jp) {
		System.out.println("----------- AspectAdviceBean after 增强  for " + jp);
	}

}
