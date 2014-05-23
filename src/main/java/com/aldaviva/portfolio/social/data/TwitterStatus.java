package com.aldaviva.portfolio.social.data;

import org.joda.time.DateTime;

public class TwitterStatus implements SocialStatus {
	
	private String body;
	private DateTime created;

	public String getBody() {
		return body;
	}
	public void setBody(final String body) {
		this.body = body;
	}
	public DateTime getCreated() {
		return created;
	}
	public void setCreated(final DateTime created) {
		this.created = created;
	}

	@Override
    public String toString() {
	    return "TwitterStatus [body=" + body + ", created=" + created + "]";
    }
	
}
