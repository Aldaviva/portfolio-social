package com.aldaviva.portfolio.social.config;

import com.aldaviva.portfolio.social.config.JacksonConfig.Jackson2Feature;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Configuration
@EnableScheduling
@ImportResource("classpath:META-INF/spring/context-property-placeholder.xml")
public class ApplicationConfig {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ApplicationConfig.class);

	private static final String MODULE_VERSION = ApplicationConfig.class.getPackage().getImplementationVersion();

	@PostConstruct
	private void init() {
		LOGGER.info("Portfolio-Social " + MODULE_VERSION + " started.");
	}

	@Bean
	public Twitter twitterClient(
	    @Value("${twitter.oauth.consumerKey}") final String consumerKey,
	    @Value("${twitter.oauth.consumerSecret}") final String consumerSecret,
	    @Value("${twitter.oauth.accessToken}") final String accessToken,
	    @Value("${twitter.oauth.accessTokenSecret}") final String accessTokenSecret) {

		final ConfigurationBuilder config = new ConfigurationBuilder();
		config.setOAuthConsumerKey(consumerKey);
		config.setOAuthConsumerSecret(consumerSecret);
		config.setOAuthAccessToken(accessToken);
		config.setOAuthAccessTokenSecret(accessTokenSecret);
		config.setTrimUserEnabled(false);
		config.setTweetModeExtended(true);
		return new TwitterFactory(config.build()).getInstance();
	}

	@Bean
	public Client httpClient() {
		final ClientConfig config = new ClientConfig();
		config.register(Jackson2Feature.class);
		config.register(UserAgentFilter.class);
		config.property(ClientProperties.CONNECT_TIMEOUT, 5000);
		config.property(ClientProperties.READ_TIMEOUT, 5000);
		config.property(ClientProperties.FOLLOW_REDIRECTS, true);
		config.connectorProvider(new ApacheConnectorProvider());
		return ClientBuilder.newClient(config);
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
		return objectMapper;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(5);
		return threadPoolTaskScheduler;
	}

	public static class UserAgentFilter implements ClientRequestFilter {

		/**
		 * Set this property to {@code false} on a request if you don't want this filter to set the {@code User-Agent} header
		 * to the default value. Useful if you don't want to send any User-Agent header at all. Note that you don't need to set this
		 * if you want to supply a custom User-Agent header value for your request, since this filter only adds the header if it 
		 * doesn't already exist.
		 */
		public static final String ADD_DEFAULT_USER_AGENT_HEADER = UserAgentFilter.class.getName() + ".addDefaultUserAgentHeader";

		public static final String DEFAULT_USER_AGENT = "com.aldaviva.portfolio.social/" + MODULE_VERSION + " +https://aldaviva.com";

		@Override
		public void filter(final ClientRequestContext requestContext) throws IOException {
			final MultivaluedMap<String, Object> requestHeaders = requestContext.getHeaders();

			// Unfortunately ClientRequestContext.getProperty(String) only returns properties explicitly set on a ClientRequestContext, it does not inherit from any higher-level
			// Configurable instances in the hierarchy, like a WebTarget, Client, or ClientConfig.
			final Boolean addDefaultUserAgentHeader = (Boolean) requestContext.getConfiguration().getProperty(ADD_DEFAULT_USER_AGENT_HEADER);
			if (!requestHeaders.containsKey(HttpHeaders.USER_AGENT) && !Boolean.FALSE.equals(addDefaultUserAgentHeader)) {
				requestHeaders.putSingle(HttpHeaders.USER_AGENT, DEFAULT_USER_AGENT);
			}
		}
	}
}
