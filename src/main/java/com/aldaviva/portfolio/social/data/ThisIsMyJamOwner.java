package com.aldaviva.portfolio.social.data;

public class ThisIsMyJamOwner implements SocialOwner {

	private String username;

	public ThisIsMyJamOwner(final String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

}
