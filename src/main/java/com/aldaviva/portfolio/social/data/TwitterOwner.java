package com.aldaviva.portfolio.social.data;

public class TwitterOwner implements SocialOwner {

	private String username;

	public TwitterOwner(final String username) {
	    this.username = username;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}
	
	
}
