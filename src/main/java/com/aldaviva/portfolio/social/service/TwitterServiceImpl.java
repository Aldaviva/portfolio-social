package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.TwitterStatus;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

@Service
public class TwitterServiceImpl implements TwitterService {

	@Inject private Twitter twitterClient;
	
	@Value("${twitter.username}") private String username;
	
	@Async
	@Override
	public ListenableFuture<TwitterStatus> getCurrentStatus() {
		final SettableFuture<TwitterStatus> future = SettableFuture.create();
		try {
			final TwitterStatus result = new TwitterStatus();
	        final User user = twitterClient.showUser(username);
	        final Status currentStatus = user.getStatus();
	        
			result.setBody(currentStatus.getText());
	        result.setCreated(new DateTime(currentStatus.getCreatedAt()));
	        future.set(result);
	        
        } catch (final twitter4j.TwitterException e) {
	        future.setException(new SocialException.TwitterException("Failed to get current Twitter status", e));
        }
		return future;
	}

}
