package com.aldaviva.portfolio.social.data;

import org.joda.time.DateTime;

public class ThisIsMyJamStatus implements SocialStatus {

	private String title;
	private String artist;
	private DateTime created;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(final String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(final String artist) {
		this.artist = artist;
	}
	public DateTime getCreated() {
		return created;
	}
	public void setCreated(final DateTime created) {
		this.created = created;
	}
	
	@Override
	public String toString() {
		return "ThisIsMyJamStatus [title=" + title + ", artist=" + artist + ", created=" + created + "]";
	}
}
