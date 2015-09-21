package com.aldaviva.portfolio.social.service.cache;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;

import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CachedSocialServiceImpl<RESULT extends SocialStatus, OWNER extends SocialOwner> implements CachedSocialService<RESULT, OWNER> {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CachedSocialServiceImpl.class);
	
	private static final long CACHE_DURATION = Period.minutes(5).toStandardDuration().getMillis();
	
	@Autowired private AutoRefreshingCache cache;
	
	@Override
    public RESULT getCachedCurrentStatus(final OWNER owner) throws SocialException{
		return cache.get(owner.getCacheKey(), new ValueGetter<RESULT>() {

			@Override
            public RESULT getValue() {
	            try {
	            	LOGGER.info("updating cache for {}", owner.getCacheKey());
	                return getCurrentStatus(owner);
                } catch (final SocialException e) {
                	LOGGER.error("Failed to get social status for "+owner.getCacheKey(), e);
                	return null;
                } 
            }
		}, CACHE_DURATION);
	}
}
