package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.FlickrException;
import com.aldaviva.portfolio.social.data.FlickrOwner;
import com.aldaviva.portfolio.social.data.FlickrStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.HttpCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.CachedHttpResult;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.Unmodified;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FlickrServiceImpl extends CachedSocialServiceImpl<FlickrStatus, FlickrOwner, HttpCacheIndicators> implements FlickrService {

	@Inject private Client webClient;
	@Inject private ObjectMapper objectMapper;

	@Value("${flickr.auth.apikey}") private String apiKey;

	private static final String ROOT_API_URL = "https://api.flickr.com/services/rest/";
	private static final String PHOTO_URL_TEMPLATE = "https://farm{farm}.static.flickr.com/{server}/{id}_{secret}_m.jpg";
	private static final String PHOTO_PAGE_URL_TEMPLATE = "https://www.flickr.com/photos/{uservanityurl}/{id}";

	@Override
	public ValueGetterResult<FlickrStatus, HttpCacheIndicators> getCurrentStatus(final FlickrOwner owner, final HttpCacheIndicators cache)
	    throws FlickrException {
		try {
			final Response response = webClient.target(ROOT_API_URL)
			    .queryParam("api_key", apiKey)
			    .queryParam("user_id", owner.getUserId())
			    .queryParam("format", "json")
			    .queryParam("nojsoncallback", "1")
			    .queryParam("method", "flickr.people.getPublicPhotos")
			    .queryParam("per_page", 1)
			    .request()
			    .header(HttpHeaders.IF_MODIFIED_SINCE, cache != null ? cache.getLastModifiedHeader() : null)
			    .header(HttpHeaders.ETAG, cache != null ? cache.getEtag() : null)
			    .get(Response.class); //won't throw WebApplicationException on 4xx or 5xx any more

			if (response.getStatus() == Status.NOT_MODIFIED.getStatusCode()) {
				response.close();
				return new Unmodified<>();
			}

			final JsonNode responseBody = response.readEntity(JsonNode.class);

			final JsonNode photoJSON = responseBody.get("photos").get("photo").get(0);
			final String thumbnailUrl = getPhotoUrl(photoJSON);
			final String photoPageUrl = getPhotoPageUrl(photoJSON, owner);
			final String title = photoJSON.get("title").textValue();

			final FlickrStatus result = new FlickrStatus();
			result.setPhotoPageUrl(photoPageUrl);
			result.setThumbnailUrl(thumbnailUrl);
			result.setTitle(title);

			final CachedHttpResult<FlickrStatus, HttpCacheIndicators> cachedHttpResult = new CachedHttpResult<>(result, new HttpCacheIndicators(response));
			response.close();
			return cachedHttpResult;
		} catch (ProcessingException | WebApplicationException e) {
			throw new SocialException.FlickrException("Failed to get current photo from Flickr", e);
		}
	}

	private String getPhotoUrl(final JsonNode photoJSON) {
		final Map<String, Object> photoMap = objectMapper.convertValue(photoJSON, new TypeReference<HashMap<String, Object>>() {
		});
		return UriBuilder.fromUri(PHOTO_URL_TEMPLATE)
		    .resolveTemplates(photoMap)
		    .build()
		    .toString();
	}

	private String getPhotoPageUrl(final JsonNode photoJSON, final FlickrOwner owner) {
		final Map<String, Object> photoMap = objectMapper.convertValue(photoJSON, new TypeReference<HashMap<String, Object>>() {
		});
		return UriBuilder.fromUri(PHOTO_PAGE_URL_TEMPLATE)
		    .resolveTemplate("uservanityurl", owner.getVanityPath())
		    .resolveTemplates(photoMap)
		    .build()
		    .toString();
	}

}
