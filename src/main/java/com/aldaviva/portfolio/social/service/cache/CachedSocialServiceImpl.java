package com.aldaviva.portfolio.social.service.cache;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;
import com.aldaviva.portfolio.social.service.ShortUrlExpanderService;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CachedSocialServiceImpl<RESULT extends SocialStatus, OWNER extends SocialOwner, CACHE extends CacheIndicators>
    implements CachedSocialService<RESULT, OWNER, CACHE> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CachedSocialServiceImpl.class);

	private static final long CACHE_DURATION = Period.minutes(5).toStandardDuration().getMillis();

	@Autowired private AutoRefreshingCache<CACHE> cache;
	@Autowired private ShortUrlExpanderService shortUrlExpanderService;

	@Override
	public RESULT getCachedCurrentStatus(final OWNER owner) throws SocialException {
		return cache.get(owner.getCacheKey(), new ValueGetter<RESULT, CACHE>() {

			@Override
			public ValueGetterResult<RESULT, CACHE> getValue(final CACHE cacheIndicators) {
				try {
					LOGGER.info("updating cache for {}", owner.getCacheKey());
					return getCurrentStatus(owner, cacheIndicators);
				} catch (final SocialException e) {
					LOGGER.error("Failed to get social status for " + owner.getCacheKey(), e);
					return null;
				}
			}
		}, CACHE_DURATION);
	}

	protected String expandShortUrls(final String body, final Collection<Pattern> shortUrlPatterns) {
		String bodyWithPreviousReplacements = body;
		final StringBuffer bodyWithNextReplacements = new StringBuffer();

		for (final Pattern pattern : shortUrlPatterns) {
			final Matcher matcher = pattern.matcher(bodyWithPreviousReplacements);

			while (matcher.find()) {
				final String shortUrl = matcher.group();
				final String expandedUrl = shortUrlExpanderService.expandShortUrl(shortUrl);
				matcher.appendReplacement(bodyWithNextReplacements, expandedUrl);
			}
			matcher.appendTail(bodyWithNextReplacements);

			bodyWithPreviousReplacements = bodyWithNextReplacements.toString();
			bodyWithNextReplacements.setLength(0);
		}

		return bodyWithPreviousReplacements;
	}
}
