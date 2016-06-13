package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.service.cache.AutoRefreshingCache;
import com.aldaviva.portfolio.social.service.cache.ValueGetter;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;
import org.glassfish.jersey.client.ClientProperties;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ShortUrlExpanderServiceImpl implements ShortUrlExpanderService {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ShortUrlExpanderServiceImpl.class);

	@Autowired private AutoRefreshingCache cache;
	@Autowired private Client webClient;

	private static final long CACHE_DURATION = Period.weeks(1).toStandardDuration().getMillis();

	@Override
	public String expandShortUrl(final String shortUrl) {
		return cache.get("shorturl." + shortUrl, new ValueGetter<String>() {
			@Override
			public String getValue() {
				return fetchLongUrl(shortUrl);
			}
		}, CACHE_DURATION);
	}

	public String fetchLongUrl(final String shortUrl) {
		String longestUrl = shortUrl;
		StatusType responseStatus;
		int redirectCountsRemaining = 10;

		try {
			boolean isRedirectionResponse;
			do {

				LOGGER.trace("Expanding URL {}", longestUrl);
				final Response response = sendRequest(longestUrl);
				response.close();

				responseStatus = response.getStatusInfo();
				isRedirectionResponse = responseStatus.getFamily().equals(Family.REDIRECTION);
				final String locationHeaderValue = response.getHeaderString(HttpHeaders.LOCATION);
				if(isRedirectionResponse && StringUtils.hasText(locationHeaderValue)) {
					LOGGER.trace("URL {} expands to {}", longestUrl, locationHeaderValue);
					longestUrl = locationHeaderValue;
				}

			} while(isRedirectionResponse && (--redirectCountsRemaining > 0));

		} catch (final ProcessingException | IllegalArgumentException e) {
			//return latest result below
		}

		LOGGER.info("Short URL {} resolves to {}", shortUrl, longestUrl);
		return longestUrl;
	}

	protected Response sendRequest(final String url) throws IllegalArgumentException {
		return webClient.target(url)
		    .property(ClientProperties.FOLLOW_REDIRECTS, false)
		    .request()
		    .head();
	}

}
