package com.aldaviva.portfolio.social.data;

public class FlickrOwner implements SocialOwner {

	private String userId;
	private String vanityPath;

	public FlickrOwner(final String userId, final String vanityPath) {
		this.userId = userId;
		this.vanityPath = vanityPath;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public String getVanityPath() {
		return vanityPath;
	}

	public void setVanityPath(final String vanityPath) {
		this.vanityPath = vanityPath;
	}

	@Override
    public String getCacheKey() {
	    return "flickr."+getUserId();
    }

}
