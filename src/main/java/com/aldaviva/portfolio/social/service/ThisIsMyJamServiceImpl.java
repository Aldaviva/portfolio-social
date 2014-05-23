package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.ThisIsMyJamStatus;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ThisIsMyJamServiceImpl implements ThisIsMyJamService {

	@Inject private Client webClient;
	
	@Value("${thisismyjam.username}") private String username;
	
	private static final String ROOT_API_URL = "http://api.thisismyjam.com/1/";
	private static final DateTimeFormatter DATETIME_PARSER = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss Z"); //Thu, 22 May 2014 22:38:14 +0000
	
	@Async
	@Override
	public ListenableFuture<ThisIsMyJamStatus> getCurrentStatus() {
		final SettableFuture<ThisIsMyJamStatus> future = SettableFuture.create();
		try {
			final JsonNode responseBody = webClient.target(ROOT_API_URL)
    			.path("{username}.json").resolveTemplate("username", username)
    			.request()
    			.get(JsonNode.class);
    		
    		final boolean hasCurrentJam = responseBody.get("person").get("hasCurrentJam").asBoolean();
    		if(hasCurrentJam){
    			final JsonNode jamObject = responseBody.get("jam");
    			final ThisIsMyJamStatus result = new ThisIsMyJamStatus();
    			result.setTitle(jamObject.get("title").textValue());
    			result.setArtist(jamObject.get("artist").textValue());
    			result.setCreated(DATETIME_PARSER.parseDateTime(jamObject.get("creationDate").textValue())); 
    			future.set(result);
    		} else {
    			future.set(null);
    		}
		} catch (ProcessingException | WebApplicationException e){
			future.setException(new SocialException.ThisIsMyJamException("Failed to get current jam from This Is My Jam", e));
		}
		
		return future;
	}
}
