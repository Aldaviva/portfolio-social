package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.service.cache.AutoRefreshingCache;
import com.aldaviva.portfolio.social.service.cache.ValueGetter;

import java.net.URI;
import java.net.URISyntaxException;
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
				String location = response.getHeaderString(HttpHeaders.LOCATION);
				if(isRedirectionResponse && StringUtils.hasText(location)) {
					try {
						final URI locationUri = new URI(location);
						if(!locationUri.isAbsolute()) {
							final URI origin = new URI(longestUrl).resolve("/");
							location = origin.resolve(locationUri).toString();
						}
						LOGGER.trace("URL {} expands to {}", longestUrl, location);
						longestUrl = location;
					} catch (final URISyntaxException e) {
						LOGGER.warn("Invalid URL {} while expanding {}, using previous URL {}", location, shortUrl, longestUrl);
						isRedirectionResponse = false;
					}
				}

			} while(isRedirectionResponse && (--redirectCountsRemaining > 0));

		} catch (final ProcessingException | IllegalArgumentException e) {
			LOGGER.warn("Error while expanding {}, using previous URL {}", shortUrl, longestUrl);
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
