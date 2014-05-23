package com.aldaviva.portfolio.social.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CompoundStatus {

	@JsonProperty("twitter")
	private TwitterStatus twitterStatus;
	
	@JsonProperty("thisismyjam")
	private ThisIsMyJamStatus thisIsMyJamStatus;
	
	@JsonProperty("flickr")
	private FlickrStatus flickrStatus;

	public TwitterStatus getTwitterStatus() {
		return twitterStatus;
	}
	public void setTwitterStatus(final TwitterStatus twitterStatus) {
		this.twitterStatus = twitterStatus;
	}
	public ThisIsMyJamStatus getThisIsMyJamStatus() {
		return thisIsMyJamStatus;
	}
	public void setThisIsMyJamStatus(final ThisIsMyJamStatus thisIsMyJamStatus) {
		this.thisIsMyJamStatus = thisIsMyJamStatus;
	}
	public FlickrStatus getFlickrStatus() {
		return flickrStatus;
	}
	public void setFlickrStatus(final FlickrStatus flickrStatus) {
		this.flickrStatus = flickrStatus;
	}
	
	@Override
	public String toString() {
		return "CompoundStatus [twitterStatus=" + twitterStatus + ", thisIsMyJamStatus=" + thisIsMyJamStatus + ", flickrStatus=" + flickrStatus + "]";
	}
}
