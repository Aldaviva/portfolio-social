package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.SocialStatus;

import com.google.common.util.concurrent.ListenableFuture;

public interface SocialService<RESULT extends SocialStatus> {

	ListenableFuture<RESULT> getCurrentStatus();
	
}
