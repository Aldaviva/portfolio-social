package com.aldaviva.portfolio.social.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

@Order(0) //run before the default Jersey SpringWebApplicationInitializer
public class SpringConfig implements WebApplicationInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringConfig.class);

	static final String PACKAGE_SCAN = "com.aldaviva.portfolio.social";

	@Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
		servletContext.setInitParameter("contextConfigLocation", ""); //prevent Jersey from also initializing Spring

		final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.scan(PACKAGE_SCAN);
		servletContext.addListener(new ContextLoaderListener(context));

		LOGGER.debug("Set up Spring context");
	}

}
