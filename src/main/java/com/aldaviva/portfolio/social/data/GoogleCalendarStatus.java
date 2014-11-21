package com.aldaviva.portfolio.social.data;

import org.joda.time.DateTime;

public class GoogleCalendarStatus implements SocialStatus {

	private String title;
	private String description;
	private DateTime startTime;
	private String location;
	private String addEventUrl;

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(final DateTime startTime) {
		this.startTime = startTime;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public String getAddEventUrl() {
		return addEventUrl;
	}

	public void setAddEventUrl(final String addEventUrl) {
		this.addEventUrl = addEventUrl;
	}

}
