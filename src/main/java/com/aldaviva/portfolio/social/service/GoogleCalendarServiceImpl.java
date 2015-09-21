package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.GoogleCalendarException;
import com.aldaviva.portfolio.social.data.GoogleCalendarOwner;
import com.aldaviva.portfolio.social.data.GoogleCalendarStatus;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.UriBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarServiceImpl.class);
	private static final String ORDERBY_STARTTIME = "startTime";

	@Autowired private Calendar calendar;

	private List<GoogleCalendarStatus> downloadEvents(final String calId) throws GoogleCalendarException {
		LOGGER.debug("Downloading calendar {}", calId);
		final List<GoogleCalendarStatus> results = new ArrayList<>();

		try {
			final Events response = calendar.events().list(calId)
				.setSingleEvents(true)
				.setOrderBy(ORDERBY_STARTTIME)
				.setTimeMin(new com.google.api.client.util.DateTime(new Date()))
				.setMaxResults(1)
				.execute();

			final List<Event> items = response.getItems();
			if(items != null){
				for (final Event event : items) {
					results.add(convertRemoteEventToCalendarEvent(event, calId));
				}
			}

			LOGGER.debug("downloaded calendar events:");
			for(final GoogleCalendarStatus event : results){
				LOGGER.debug(event.toString());
			}

			return results;

		} catch (final IOException e) {
			throw new SocialException.GoogleCalendarException(e);
		}
	}
	
	private GoogleCalendarStatus convertRemoteEventToCalendarEvent(final Event remoteEvent, final String calId){
		LOGGER.debug("Converting remote event {}", remoteEvent);
		
		final GoogleCalendarStatus result = new GoogleCalendarStatus();
		result.setTitle(remoteEvent.getSummary());
		result.setDescription(remoteEvent.getDescription());
		result.setLocation(remoteEvent.getLocation());
		
		com.google.api.client.util.DateTime googleStartDate = remoteEvent.getStart().getDateTime();
		if(googleStartDate == null){
			googleStartDate = remoteEvent.getStart().getDate();
			result.setStartTime(new DateTime(googleStartDate.getValue(), DateTimeZone.forOffsetHours(googleStartDate.getTimeZoneShift())).withZoneRetainFields(DateTimeZone.getDefault()));
		} else {
			result.setStartTime(new DateTime(googleStartDate.getValue()));
		}
		
		final String eid = UriComponentsBuilder.fromHttpUrl(remoteEvent.getHtmlLink()).build().getQueryParams().getFirst("eid");
		final String addEventUrl = UriBuilder.fromUri("https://www.google.com/calendar/event")
			.queryParam("action", "TEMPLATE")
			.queryParam("tmeid", eid)
			.queryParam("tmsrc", calId)
			.build().toString();
		result.setAddEventUrl(addEventUrl);
		
		return result;
	}

	@Override
    public GoogleCalendarStatus getCurrentStatus(final GoogleCalendarOwner owner) throws GoogleCalendarException {
		final List<GoogleCalendarStatus> events = downloadEvents(owner.getCalendarId());
		if(!events.isEmpty()){
			return events.get(0);
		} else {
			throw new SocialException.GoogleCalendarException.NoCalendarEventsFound();
		}
    }

}