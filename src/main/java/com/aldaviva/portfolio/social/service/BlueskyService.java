package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.BlueskyOwner;
import com.aldaviva.portfolio.social.data.BlueskyStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialService;

public interface BlueskyService extends CachedSocialService<BlueskyStatus, BlueskyOwner, CacheIndicators.None> {

}
