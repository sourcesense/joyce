//package com.sourcesense.joyce.core.condition;
//
//import ch.qos.logback.core.Context;
//import ch.qos.logback.core.spi.ContextAware;
//import ch.qos.logback.core.status.Status;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Condition;
//import org.springframework.context.annotation.ConditionContext;
//import org.springframework.core.type.AnnotatedTypeMetadata;
//
//public class MissingBeanCondition implements Condition, ApplicationContextAware {
//
//	private ApplicationContext applicationContext;
//
//	@Override
//	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
//		applicationContext
//	}
//
//	@Override
//	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//		this.applicationContext = applicationContext;
//	}
//}
