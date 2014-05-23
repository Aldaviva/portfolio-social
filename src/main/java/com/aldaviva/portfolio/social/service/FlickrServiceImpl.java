package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.FlickrStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FlickrServiceImpl implements FlickrService {

	@Inject private Client webClient;
	@Inject private ObjectMapper objectMapper;
	
	@Value("${flickr.userid}") private String userId;
	@Value("${flickr.uservanityurl}") private String userVanityUrl;
	@Value("${flickr.auth.apikey}") private String apiKey;
	
	private static final String ROOT_API_URL = "https://api.flickr.com/services/rest/";
	private static final String PHOTO_URL_TEMPLATE = "http://farm{farm}.static.flickr.com/{server}/{id}_{secret}_m.jpg";
	private static final String PHOTO_PAGE_URL_TEMPLATE = "http://www.flickr.com/photos/{uservanityurl}/{id}";
	
	@Async
	@Override
	public ListenableFuture<FlickrStatus> getCurrentStatus() {
		final SettableFuture<FlickrStatus> future = SettableFuture.create();
		
		try {
    		final JsonNode responseBody = webClient.target(ROOT_API_URL)
    			.queryParam("api_key", apiKey)
    			.queryParam("user_id", userId)
    			.queryParam("format", "json")
    			.queryParam("nojsoncallback", "1")
    			.queryParam("method", "flickr.people.getPublicPhotos")
    			.queryParam("per_page", 1)
    			.request()
    			.get(JsonNode.class);
    		
    		final JsonNode photoJSON = responseBody.get("photos").get("photo").get(0);
    		final String thumbnailUrl = getPhotoUrl(photoJSON);
    		final String photoPageUrl = getPhotoPageUrl(photoJSON);
    		final String title = photoJSON.get("title").textValue();
    		
    		final FlickrStatus result = new FlickrStatus();
    		result.setPhotoPageUrl(photoPageUrl);
    		result.setThumbnailUrl(thumbnailUrl);
    		result.setTitle(title);
    		future.set(result);
		} catch (ProcessingException | WebApplicationException e){
			future.setException(new SocialException.FlickrException("Failed to get current photo from Flickr", e));
		}
		return future;
	}

	private String getPhotoUrl(final JsonNode photoJSON){
		try {
			final Map<String, Object> photoMap = objectMapper.treeToValue(photoJSON, HashMap.class);
	        return UriBuilder.fromUri(PHOTO_URL_TEMPLATE)
	        	.resolveTemplates(photoMap)
	        	.build()
	        	.toString();
        } catch (final JsonProcessingException e) {
	        throw new RuntimeException(e);
        }
	}
	
	private String getPhotoPageUrl(final JsonNode photoJSON){
        try {
        	final Map<String, Object> photoMap = objectMapper.treeToValue(photoJSON, HashMap.class);
    		return UriBuilder.fromUri(PHOTO_PAGE_URL_TEMPLATE)
    			.resolveTemplate("uservanityurl", userVanityUrl)
    			.resolveTemplates(photoMap)
    			.build()
    			.toString();
        } catch (final JsonProcessingException e) {
        	throw new RuntimeException(e);
        }
	}

}
