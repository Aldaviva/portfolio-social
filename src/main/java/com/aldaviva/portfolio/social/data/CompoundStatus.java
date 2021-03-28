package com.aldaviva.portfolio.social.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CompoundStatus {

	@JsonProperty("twitter") private TwitterStatus twitterStatus;

	@JsonProperty("flickr") private FlickrStatus flickrStatus;

	public TwitterStatus getTwitterStatus() {
		return twitterStatus;
	}

	public void setTwitterStatus(final TwitterStatus twitterStatus) {
		this.twitterStatus = twitterStatus;
	}

	public FlickrStatus getFlickrStatus() {
		return flickrStatus;
	}

	public void setFlickrStatus(final FlickrStatus flickrStatus) {
		this.flickrStatus = flickrStatus;
	}

	@Override
	public String toString() {
		return "CompoundStatus [twitterStatus=" + twitterStatus + ", flickrStatus=" + flickrStatus + "]";
	}
}
