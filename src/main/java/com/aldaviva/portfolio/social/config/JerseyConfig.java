package com.aldaviva.portfolio.social.config;

import com.aldaviva.portfolio.social.config.JacksonConfig.Jackson2Feature;

import java.io.IOException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath(JerseyConfig.ROOT_PATH)
public class JerseyConfig extends ResourceConfig {

	public static final String ROOT_PATH = "api";

	public JerseyConfig() {
		//		register(SpringConfig.context.getBean(JacksonConfig.class));
		register(Jackson2Feature.class);
		packages(SpringConfig.PACKAGE_SCAN);

		registerInstances(new CrossOriginFilter());
	}

	private static final class CrossOriginFilter implements ContainerResponseFilter {

		@Override
		public void filter(final ContainerRequestContext req, final ContainerResponseContext res) throws IOException {
			res.getHeaders().add("Access-Control-Allow-Origin", "*");
		}
	}
}
