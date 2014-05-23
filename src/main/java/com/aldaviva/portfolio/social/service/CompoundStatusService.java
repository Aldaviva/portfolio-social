package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.CompoundStatus;

import com.google.common.util.concurrent.ListenableFuture;

public interface CompoundStatusService {

	ListenableFuture<CompoundStatus> getCompoundStatus();

}
