package com.aldaviva.portfolio.social.service.cache;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;
import com.aldaviva.portfolio.social.service.SocialService;

public interface CachedSocialService<RESULT extends SocialStatus, OWNER extends SocialOwner, CACHE extends CacheIndicators>
    extends SocialService<RESULT, OWNER, CACHE> {

	RESULT getCachedCurrentStatus(OWNER owner) throws SocialException;

}