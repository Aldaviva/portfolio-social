package com.aldaviva.portfolio.social.data;

public class GithubOwner implements SocialOwner {

	private String username;

	public GithubOwner(final String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public String getCacheKey() {
		return "github." + username;
	}

}
