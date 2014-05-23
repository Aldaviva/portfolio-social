package com.aldaviva.portfolio.social.config;

import com.aldaviva.portfolio.social.config.JacksonConfig.Jackson2Feature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Configuration
@EnableScheduling
@EnableAsync
@ImportResource("classpath:META-INF/spring/context-property-placeholder.xml")
public class ApplicationConfig implements AsyncConfigurer {

	@Bean
	public Twitter twitterClient(
	    @Value("${twitter.oauth.consumerKey}") final String consumerKey,
	    @Value("${twitter.oauth.consumerSecret}") final String consumerSecret,
	    @Value("${twitter.oauth.accessToken}") final String accessToken,
	    @Value("${twitter.oauth.accessTokenSecret}") final String accessTokenSecret
	    ) {
		final ConfigurationBuilder config = new ConfigurationBuilder();
		config.setOAuthConsumerKey(consumerKey);
		config.setOAuthConsumerSecret(consumerSecret);
		config.setOAuthAccessToken(accessToken);
		config.setOAuthAccessTokenSecret(accessTokenSecret);
		return new TwitterFactory(config.build()).getInstance();
	}

	@Bean
	public Client httpClient() {
		final ClientConfig config = new ClientConfig();
		config.register(Jackson2Feature.class);
		config.property(ClientProperties.CONNECT_TIMEOUT, 5000);
		config.property(ClientProperties.READ_TIMEOUT, 5000);
		config.connectorProvider(new ApacheConnectorProvider());
		return ClientBuilder.newClient(config);
	}

	@Bean
	public ObjectMapper objectMapper() {
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JodaModule());
		return objectMapper;
	}

	@Bean
	public ListeningExecutorService executorService() {
		return MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	}

	@Override
	public Executor getAsyncExecutor() {
		return executorService();
	}

}
