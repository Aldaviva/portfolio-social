package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.FlickrException;
import com.aldaviva.portfolio.social.data.FlickrOwner;
import com.aldaviva.portfolio.social.data.FlickrStatus;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FlickrServiceImpl extends CachedSocialServiceImpl<FlickrStatus, FlickrOwner> implements FlickrService {

	@Inject private Client webClient;
	@Inject private ObjectMapper objectMapper;

//	@Value("${flickr.userid}") private String userId;
//	@Value("${flickr.uservanityurl}") private String userVanityUrl;
	@Value("${flickr.auth.apikey}") private String apiKey;

	private static final String ROOT_API_URL = "https://api.flickr.com/services/rest/";
	private static final String PHOTO_URL_TEMPLATE = "https://farm{farm}.static.flickr.com/{server}/{id}_{secret}_m.jpg";
	private static final String PHOTO_PAGE_URL_TEMPLATE = "https://www.flickr.com/photos/{uservanityurl}/{id}";

	@Override
	public FlickrStatus getCurrentStatus(final FlickrOwner owner) throws FlickrException {
		try {
			final JsonNode responseBody = webClient.target(ROOT_API_URL)
			    .queryParam("api_key", apiKey)
			    .queryParam("user_id", owner.getUserId())
			    .queryParam("format", "json")
			    .queryParam("nojsoncallback", "1")
			    .queryParam("method", "flickr.people.getPublicPhotos")
			    .queryParam("per_page", 1)
			    .request()
			    .get(JsonNode.class);

			final JsonNode photoJSON = responseBody.get("photos").get("photo").get(0);
			final String thumbnailUrl = getPhotoUrl(photoJSON);
			final String photoPageUrl = getPhotoPageUrl(photoJSON, owner);
			final String title = photoJSON.get("title").textValue();

			final FlickrStatus result = new FlickrStatus();
			result.setPhotoPageUrl(photoPageUrl);
			result.setThumbnailUrl(thumbnailUrl);
			result.setTitle(title);
			return result;
		} catch (ProcessingException | WebApplicationException e) {
			throw new SocialException.FlickrException("Failed to get current photo from Flickr", e);
		}
	}

	private String getPhotoUrl(final JsonNode photoJSON) {
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

	private String getPhotoPageUrl(final JsonNode photoJSON, final FlickrOwner owner) {
		try {
			final Map<String, Object> photoMap = objectMapper.treeToValue(photoJSON, HashMap.class);
			return UriBuilder.fromUri(PHOTO_PAGE_URL_TEMPLATE)
			    .resolveTemplate("uservanityurl", owner.getVanityPath())
			    .resolveTemplates(photoMap)
			    .build()
			    .toString();
		} catch (final JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
