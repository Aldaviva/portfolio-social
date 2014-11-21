package com.aldaviva.portfolio.social.api;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.FlickrOwner;
import com.aldaviva.portfolio.social.data.FlickrStatus;
import com.aldaviva.portfolio.social.data.GoogleCalendarOwner;
import com.aldaviva.portfolio.social.data.GoogleCalendarStatus;
import com.aldaviva.portfolio.social.data.ThisIsMyJamOwner;
import com.aldaviva.portfolio.social.data.ThisIsMyJamStatus;
import com.aldaviva.portfolio.social.data.TwitterOwner;
import com.aldaviva.portfolio.social.data.TwitterStatus;
import com.aldaviva.portfolio.social.service.FlickrService;
import com.aldaviva.portfolio.social.service.GoogleCalendarService;
import com.aldaviva.portfolio.social.service.ThisIsMyJamService;
import com.aldaviva.portfolio.social.service.TwitterService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

@Path("social")
@Component
@Produces({ MediaType.APPLICATION_JSON })
public class SocialResource {

	@Inject private TwitterService twitterService;
	@Inject private FlickrService flickrService;
	@Inject private ThisIsMyJamService thisIsMyJamService;
	@Inject private GoogleCalendarService googleCalendarService;
	
	@GET
	@Path("twitter/{username}")
	public TwitterStatus getTwitterStatus(@PathParam("username") final String username) throws SocialException{
		return twitterService.getCurrentStatus(new TwitterOwner(username));
	}
	
	@GET
	@Path("flickr/{userid}/{vanitypath}")
	public FlickrStatus getFlickrStatus(@PathParam("userid") final String userId, @PathParam("vanitypath") final String vanityPath) throws SocialException{
		return flickrService.getCurrentStatus(new FlickrOwner(userId, vanityPath));
	}
	
	@GET
	@Path("thisismyjam/{username}")
	public ThisIsMyJamStatus getThisIsMyJamStatus(@PathParam("username") final String username) throws SocialException{
		return thisIsMyJamService.getCurrentStatus(new ThisIsMyJamOwner(username));
	}
	
	@GET
	@Path("googlecalendar/{calendarId}")
	public GoogleCalendarStatus getGoogleCalendarStatus(@PathParam("calendarId") final String calendarId) throws SocialException{
		return googleCalendarService.getCurrentStatus(new GoogleCalendarOwner(calendarId));
	}
}
