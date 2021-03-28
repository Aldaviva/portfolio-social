package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

public interface SocialService<RESULT extends SocialStatus, OWNER extends SocialOwner, CACHE extends CacheIndicators> {

	ValueGetterResult<RESULT, CACHE> getCurrentStatus(OWNER owner, CACHE cacheIndicators) throws SocialException;

}
