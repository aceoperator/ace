/**
 * 
 */
package com.quikj.application.communicator.admin.controller;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author amit
 * 
 */
public class SpringUtils {
	private static final String SPRING_CONTEXT = "springContext";

	public static void initSpringContext(ServletContext context) {
		ApplicationContext springContext = WebApplicationContextUtils
				.getWebApplicationContext(context);
		context.setAttribute(SpringUtils.SPRING_CONTEXT, springContext);
	}

	public static Object getBean(ServletContext context, String beanName) {
		ApplicationContext springContext = (ApplicationContext) context
				.getAttribute(SPRING_CONTEXT);
		return springContext.getBean(beanName);
	}

	public static <T> T getBean(ServletContext context, Class<T> beanClass) {
		ApplicationContext springContext = (ApplicationContext) context
				.getAttribute(SPRING_CONTEXT);
		return springContext.getBean(beanClass);
	}
}
