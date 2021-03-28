package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.FlickrOwner;
import com.aldaviva.portfolio.social.data.FlickrStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.HttpCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialService;

public interface FlickrService extends CachedSocialService<FlickrStatus, FlickrOwner, HttpCacheIndicators> {

}
