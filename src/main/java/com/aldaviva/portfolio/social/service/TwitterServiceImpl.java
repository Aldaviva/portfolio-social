package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.TwitterException;
import com.aldaviva.portfolio.social.data.TwitterOwner;
import com.aldaviva.portfolio.social.data.TwitterStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.None;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.NonCachingResult;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.regex.Pattern;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

@Service
public class TwitterServiceImpl extends CachedSocialServiceImpl<TwitterStatus, TwitterOwner, CacheIndicators.None> implements TwitterService {

	private static Collection<Pattern> SHORT_URL_PATTERNS = ImmutableList.of(Pattern.compile("https:\\/\\/t\\.co\\/\\w+"));

	@Inject private Twitter twitterClient;

	@Override
	public ValueGetterResult<TwitterStatus, None> getCurrentStatus(final TwitterOwner owner, final CacheIndicators.None none) throws TwitterException {
		try {
			final TwitterStatus twitterStatus = new TwitterStatus();

			final User userProfile = twitterClient.users().showUser(owner.getUsername());
			final Status status = userProfile.getStatus();

			if (status != null) {
				twitterStatus.setBody(expandShortUrls(status.getText(), SHORT_URL_PATTERNS));
				twitterStatus.setCreated(new DateTime(status.getCreatedAt()));
				return new NonCachingResult<>(twitterStatus);
			} else {
				throw new SocialException.TwitterException("Could not get most recent tweet from " + owner.getUsername());
			}

		} catch (final twitter4j.TwitterException e) {
			throw new SocialException.TwitterException("Failed to get current Twitter status", e);
		}
	}

}
