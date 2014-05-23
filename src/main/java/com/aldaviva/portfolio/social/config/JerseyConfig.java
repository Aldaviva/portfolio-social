package com.aldaviva.portfolio.social.config;

import com.aldaviva.portfolio.social.config.JacksonConfig.Jackson2Feature;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath(JerseyConfig.ROOT_PATH)
public class JerseyConfig extends ResourceConfig {

	public static final String ROOT_PATH = "api";

	public JerseyConfig() {
		register(Jackson2Feature.class);
		packages(SpringConfig.PACKAGE_SCAN);
	}
}
