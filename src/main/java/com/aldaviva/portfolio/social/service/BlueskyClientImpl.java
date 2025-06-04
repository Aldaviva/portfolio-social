package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.service.BlueskySchema.PaginatedFeed;
import com.aldaviva.portfolio.social.service.BlueskySchema.Post;

import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import org.springframework.stereotype.Component;

@Component
public class BlueskyClientImpl implements BlueskyClient {

	public static final URI API_BASE = URI.create("https://public.api.bsky.app/xrpc/");

	@Inject private Client httpClient;
	// You can make anonymous requests to app.bsky.feed.getAuthorFeed as long as you use the special Public API root URL
	//	@Inject private BlueskyAuthenticationFilter authFilter;

	protected WebTarget target(final String namespaceId) {
		return httpClient.target(API_BASE)
		    .path(namespaceId);
		//		    .register(authFilter);
	}

	/**
	 * @see https://docs.bsky.app/docs/api/app-bsky-feed-get-author-feed
	 * @see https://github.com/bluesky-social/atproto/blob/main/lexicons/app/bsky/feed/getAuthorFeed.json
	 */
	@Override
	public Post getLatestPost(final String username) {
		return target("app.bsky.feed.getAuthorFeed")
		    .queryParam("actor", username)
		    .queryParam("limit", 1)
		    .queryParam("filter", "posts_no_replies")
		    .queryParam("includePins", false)
		    .request()
		    .get(PaginatedFeed.class).feed
		        .get(0).post;
	}

}
