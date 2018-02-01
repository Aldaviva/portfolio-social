package com.aldaviva.portfolio.social.service;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.aldaviva.portfolio.social.AbstractInjectedTest;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShortUrlExpanderServiceTest extends AbstractInjectedTest {

	@Spy private ShortUrlExpanderServiceImpl shortUrlExpanderService;

	@BeforeMethod
	private void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void singleSuccessfulRedirection() {
		final String shortUrl = "https://t.co/pIqtyHjKVB";
		final String longUrl = "https://www.youtube.com/watch?v=Zi_TMwC_WyI&feature=youtu.be";

		final Response response1 = mock(Response.class);
		doReturn(response1).when(shortUrlExpanderService).sendRequest(eq(shortUrl));
		when(response1.getStatusInfo()).thenReturn(Status.MOVED_PERMANENTLY);
		when(response1.getHeaderString(HttpHeaders.LOCATION)).thenReturn(longUrl);

		final Response response2 = mock(Response.class);
		doReturn(response2).when(shortUrlExpanderService).sendRequest(eq(longUrl));
		when(response2.getStatusInfo()).thenReturn(Status.OK);
		when(response2.getHeaderString(HttpHeaders.LOCATION)).thenReturn(null);

		final String actual = shortUrlExpanderService.fetchLongUrl(shortUrl);

		assertEquals(actual, longUrl);

		verify(shortUrlExpanderService).sendRequest(eq(shortUrl));
		verify(response1).close();

		verify(shortUrlExpanderService).sendRequest(eq(longUrl));
		verify(response2).close();
	}

	@Test
	public void singleFailedRedirection() {
		final String shortUrl = "https://t.co/pIqtyHjKVB";

		final Response response1 = mock(Response.class);
		doReturn(response1).when(shortUrlExpanderService).sendRequest(eq(shortUrl));
		when(response1.getStatusInfo()).thenReturn(Status.NOT_FOUND);
		when(response1.getHeaderString(HttpHeaders.LOCATION)).thenReturn(null);

		final String actual = shortUrlExpanderService.fetchLongUrl(shortUrl);

		assertEquals(actual, shortUrl);

		verify(shortUrlExpanderService).sendRequest(eq(shortUrl));
		verify(response1).close();
	}

	@Test
	public void doubleSuccessfulRedirection() {
		final String shortUrl = "https://t.co/pIqtyHjKVB";
		final String mediumUrl = "https://youtu.be/Zi_TMwC_WyI";
		final String longUrl = "https://www.youtube.com/watch?v=Zi_TMwC_WyI&feature=youtu.be";

		final Response response1 = mock(Response.class);
		doReturn(response1).when(shortUrlExpanderService).sendRequest(eq(shortUrl));
		when(response1.getStatusInfo()).thenReturn(Status.MOVED_PERMANENTLY);
		when(response1.getHeaderString(HttpHeaders.LOCATION)).thenReturn(mediumUrl);

		final Response response2 = mock(Response.class);
		doReturn(response2).when(shortUrlExpanderService).sendRequest(eq(mediumUrl));
		when(response2.getStatusInfo()).thenReturn(Status.MOVED_PERMANENTLY);
		when(response2.getHeaderString(HttpHeaders.LOCATION)).thenReturn(longUrl);

		final Response response3 = mock(Response.class);
		doReturn(response3).when(shortUrlExpanderService).sendRequest(eq(longUrl));
		when(response3.getStatusInfo()).thenReturn(Status.OK);
		when(response3.getHeaderString(HttpHeaders.LOCATION)).thenReturn(null);

		final String actual = shortUrlExpanderService.fetchLongUrl(shortUrl);

		assertEquals(actual, longUrl);

		verify(shortUrlExpanderService).sendRequest(eq(shortUrl));
		verify(response1).close();

		verify(shortUrlExpanderService).sendRequest(eq(mediumUrl));
		verify(response2).close();

		verify(shortUrlExpanderService).sendRequest(eq(longUrl));
		verify(response3).close();
	}

	@Test
	public void relativeRedirection() {
		final String[] absoluteUrls = new String[] {
		    "https://t.co/0uZCakB1Vi",
		    "https://flic.kr/p/23NrJtU",
		    "https://www.flickr.com/photo.gne?short=23NrJtU",
		    "https://www.flickr.com/photo.gne?rb=1&short=23NrJtU",
		    "https://www.flickr.com/photos/benhutchison/39906985602/"
		};

		final String[] locationHeaders = new String[] {
		    "https://t.co/0uZCakB1Vi",
		    "https://flic.kr/p/23NrJtU",
		    "https://www.flickr.com/photo.gne?short=23NrJtU",
		    "/photo.gne?rb=1&short=23NrJtU",
		    "/photos/benhutchison/39906985602/"
		};

		final Response[] responses = new Response[absoluteUrls.length];

		for(int i = 0; i < absoluteUrls.length; i++) {
			final String requestUrl = absoluteUrls[i];
			final Response response = mock(Response.class);
			doReturn(response).when(shortUrlExpanderService).sendRequest(eq(requestUrl));

			if(locationHeaders.length > i + 1) {
				final String nextUrl = locationHeaders[i + 1];
				when(response.getStatusInfo()).thenReturn(Status.MOVED_PERMANENTLY);
				when(response.getHeaderString(HttpHeaders.LOCATION)).thenReturn(nextUrl);
			} else {
				when(response.getStatusInfo()).thenReturn(Status.OK);
				when(response.getHeaderString(HttpHeaders.LOCATION)).thenReturn(null);
			}

			responses[i] = response;
		}

		final String actual = shortUrlExpanderService.fetchLongUrl(absoluteUrls[0]);
		assertEquals(actual, absoluteUrls[absoluteUrls.length - 1]);

		for(int i = 0; i < absoluteUrls.length; i++) {
			verify(shortUrlExpanderService).sendRequest(eq(absoluteUrls[i]));
			verify(responses[i]).close();
		}
	}
}
