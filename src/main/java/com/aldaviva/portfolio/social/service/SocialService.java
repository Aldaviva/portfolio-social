package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;

public interface SocialService<RESULT extends SocialStatus, OWNER extends SocialOwner> {

	RESULT getCurrentStatus(OWNER owner) throws SocialException;
	
}
