package com.aldaviva.portfolio.social.config;

import com.aldaviva.portfolio.social.config.JacksonConfig.Jackson2Feature;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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
		config.setTrimUserEnabled(true);
		config.setTweetModeExtended(true);
		return new TwitterFactory(config.build()).getInstance();
	}

	@Bean
	public Client httpClient() {
		final ClientConfig config = new ClientConfig();
		config.register(Jackson2Feature.class);
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

}
