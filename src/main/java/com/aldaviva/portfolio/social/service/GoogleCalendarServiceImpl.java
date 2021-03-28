package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.GoogleCalendarException;
import com.aldaviva.portfolio.social.data.GoogleCalendarOwner;
import com.aldaviva.portfolio.social.data.GoogleCalendarStatus;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.HttpCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.CachedHttpResult;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

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
public class GoogleCalendarServiceImpl extends CachedSocialServiceImpl<GoogleCalendarStatus, GoogleCalendarOwner, HttpCacheIndicators>
    implements GoogleCalendarService {

	private static final Logger LOGGER = LoggerFactory.getLogger(GoogleCalendarServiceImpl.class);
	private static final String ORDERBY_STARTTIME = "startTime";

	@Autowired private Calendar calendar;

	private ValueGetterResult<GoogleCalendarStatus, HttpCacheIndicators> downloadEvents(final String calId, final HttpCacheIndicators cache)
	    throws GoogleCalendarException {
		LOGGER.debug("Downloading calendar {}", calId);
		final List<GoogleCalendarStatus> results = new ArrayList<>();

		try {
			final com.google.api.services.calendar.Calendar.Events.List requestBuilder = calendar.events().list(calId)
			    .setSingleEvents(true)
			    .setOrderBy(ORDERBY_STARTTIME)
			    .setTimeMin(new com.google.api.client.util.DateTime(new Date()))
			    .setMaxResults(1)
			    .setUpdatedMin(
			        cache != null && cache.getLastModified() != null ? new com.google.api.client.util.DateTime(cache.getLastModified().getMillis()) : null);
			requestBuilder.getRequestHeaders().setETag(cache != null ? cache.getEtag() : null);
			final Events response = requestBuilder.execute();

			final List<Event> items = response.getItems();
			if (items != null) {
				for (final Event event : items) {
					results.add(convertRemoteEventToCalendarEvent(event, calId));
				}
			}

			LOGGER.debug("downloaded calendar events:");
			if (LOGGER.isDebugEnabled()) {
				for (final GoogleCalendarStatus event : results) {
					LOGGER.debug(event.toString());
				}
			}

			final HttpCacheIndicators responseCacheIndicators = new HttpCacheIndicators();
			responseCacheIndicators.setEtag(response.getEtag());
			responseCacheIndicators.setLastModified(response.getUpdated() != null ? new DateTime(response.getUpdated().getValue()) : null);

			if (!results.isEmpty()) {
				return new CachedHttpResult<>(results.get(0), responseCacheIndicators);
			} else {
				return null;
			}

		} catch (final IOException e) {
			throw new SocialException.GoogleCalendarException(e);
		}
	}

	private GoogleCalendarStatus convertRemoteEventToCalendarEvent(final Event remoteEvent, final String calId) {
		LOGGER.debug("Converting remote event {}", remoteEvent);

		final GoogleCalendarStatus result = new GoogleCalendarStatus();
		result.setTitle(remoteEvent.getSummary());
		result.setDescription(remoteEvent.getDescription());
		result.setLocation(remoteEvent.getLocation());

		com.google.api.client.util.DateTime googleStartDate = remoteEvent.getStart().getDateTime();
		if (googleStartDate == null) {
			googleStartDate = remoteEvent.getStart().getDate();
			result.setStartTime(new DateTime(googleStartDate.getValue(), DateTimeZone.forOffsetHours(googleStartDate.getTimeZoneShift()))
			    .withZoneRetainFields(DateTimeZone.getDefault()));
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
	public ValueGetterResult<GoogleCalendarStatus, HttpCacheIndicators> getCurrentStatus(
	    final GoogleCalendarOwner owner, final HttpCacheIndicators cacheIndicators) throws GoogleCalendarException {

		final ValueGetterResult<GoogleCalendarStatus, HttpCacheIndicators> events = downloadEvents(owner.getCalendarId(), cacheIndicators);
		if (events != null) {
			return events;
		} else {
			throw new SocialException.GoogleCalendarException.NoCalendarEventsFound();
		}
	}

}