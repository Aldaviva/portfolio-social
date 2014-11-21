package com.aldaviva.portfolio.social.data;

public class GoogleCalendarOwner implements SocialOwner {

	private String calendarId;

	public GoogleCalendarOwner(final String calendarId) {
		this.calendarId = calendarId;
	}

	public String getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(final String calendarId) {
		this.calendarId = calendarId;
	}

}
