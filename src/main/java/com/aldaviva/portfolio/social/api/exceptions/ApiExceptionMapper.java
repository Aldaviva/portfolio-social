package com.aldaviva.portfolio.social.api.exceptions;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.common.exceptions.SocialException.GoogleCalendarException;

import java.util.Collections;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<SocialException> {

	@Override
    public Response toResponse(final SocialException socialException) {
	    if(socialException instanceof GoogleCalendarException.NoCalendarEventsFound){
	    	return buildResponse(404, "no calendar events found");
	    } else {
	    	return buildResponse(500, socialException.getMessage());
	    }
    }
	
	private Response buildResponse(final int code, final String message){
		return Response.status(code)
    		.entity(Collections.singletonMap("error", message))
    		.type(MediaType.APPLICATION_JSON_TYPE)
    		.build();
	}

	
	
}
