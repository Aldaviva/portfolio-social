package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.BlueskyOwner;
import com.aldaviva.portfolio.social.data.BlueskyStatus;
import com.aldaviva.portfolio.social.service.BlueskySchema.Post;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.None;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.NonCachingResult;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import org.springframework.stereotype.Service;

@Service
public class BlueskyServiceImpl extends CachedSocialServiceImpl<BlueskyStatus, BlueskyOwner, CacheIndicators.None> implements BlueskyService {

	@Inject private BlueskyClient bluesky;

	@Override
	public ValueGetterResult<BlueskyStatus, None> getCurrentStatus(final BlueskyOwner owner, final None cacheIndicators) throws SocialException {
		final BlueskyStatus blueskyStatus = new BlueskyStatus();
		try {
			final Post status = bluesky.getLatestPost(owner.getUsername());
			blueskyStatus.setBody(status.record.text);
			blueskyStatus.setCreated(status.record.createdAt);
			return new NonCachingResult<>(blueskyStatus);
		} catch (WebApplicationException | ProcessingException e) {
			throw new SocialException("Failed to get latest Bluesky post for user " + owner.getUsername(), e);
		}
	}

}
