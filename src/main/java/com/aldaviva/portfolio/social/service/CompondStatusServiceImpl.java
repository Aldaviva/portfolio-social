package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.CompoundStatus;
import com.aldaviva.portfolio.social.data.FlickrStatus;
import com.aldaviva.portfolio.social.data.SocialStatus;
import com.aldaviva.portfolio.social.data.ThisIsMyJamStatus;
import com.aldaviva.portfolio.social.data.TwitterStatus;

import com.google.common.base.Function;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class CompondStatusServiceImpl implements CompoundStatusService {

	@Inject private TwitterService twitterService;
	@Inject private ThisIsMyJamService thisIsMyJamService;
	@Inject private FlickrService flickrService;
	
	@Override
	public ListenableFuture<CompoundStatus> getCompoundStatus(){
		final CompoundStatus result = new CompoundStatus();
		
		final ListenableFuture<TwitterStatus> twitterStatusFuture = twitterService.getCurrentStatus();
		final ListenableFuture<ThisIsMyJamStatus> thisIsMyJamStatusFuture = thisIsMyJamService.getCurrentStatus();
		final ListenableFuture<FlickrStatus> flickrStatusFuture = flickrService.getCurrentStatus();
		
		Futures.addCallback(twitterStatusFuture, new FutureCallback<TwitterStatus>(){
			@Override public void onSuccess(final TwitterStatus status) {
				result.setTwitterStatus(status);
			}
			@Override public void onFailure(final Throwable t) { }
		});
		
		Futures.addCallback(thisIsMyJamStatusFuture, new FutureCallback<ThisIsMyJamStatus>(){
			@Override public void onSuccess(final ThisIsMyJamStatus status) {
				result.setThisIsMyJamStatus(status);
			}
			@Override public void onFailure(final Throwable t) { }
		});
		
		Futures.addCallback(flickrStatusFuture, new FutureCallback<FlickrStatus>(){
			@Override public void onSuccess(final FlickrStatus status) {
				result.setFlickrStatus(status);
			}
			@Override public void onFailure(final Throwable t) { }
		});
		
		final ListenableFuture<List<SocialStatus>> allFutures = Futures.allAsList(Arrays.asList(twitterStatusFuture, thisIsMyJamStatusFuture, flickrStatusFuture));
		
		return Futures.transform(allFutures, new Function<List<? extends SocialStatus>, CompoundStatus>(){
			@Override public CompoundStatus apply(final List<? extends SocialStatus> input) {
				return result;
			}
		});
	}
	
}
