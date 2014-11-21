package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.TwitterException;
import com.aldaviva.portfolio.social.data.TwitterOwner;
import com.aldaviva.portfolio.social.data.TwitterStatus;

import javax.inject.Inject;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

@Service
public class TwitterServiceImpl implements TwitterService {

	@Inject private Twitter twitterClient;

	@Override
	public TwitterStatus getCurrentStatus(final TwitterOwner owner) throws TwitterException {
		try {
			final TwitterStatus result = new TwitterStatus();
			final User user = twitterClient.showUser(owner.getUsername());
			final Status currentStatus = user.getStatus();

			result.setBody(currentStatus.getText());
			result.setCreated(new DateTime(currentStatus.getCreatedAt()));
			return result;

		} catch (final twitter4j.TwitterException e) {
			throw new SocialException.TwitterException("Failed to get current Twitter status", e);
		}
	}

}
