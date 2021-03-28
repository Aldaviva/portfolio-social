package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.TwitterOwner;
import com.aldaviva.portfolio.social.data.TwitterStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialService;

public interface TwitterService extends CachedSocialService<TwitterStatus, TwitterOwner, CacheIndicators.None> {

}
