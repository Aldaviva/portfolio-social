package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.service.BlueskySchema.Post;

public interface BlueskyClient {

	Post getLatestPost(String username);

}