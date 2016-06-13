package com.aldaviva.portfolio.social.service.cache;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.SocialOwner;
import com.aldaviva.portfolio.social.data.SocialStatus;
import com.aldaviva.portfolio.social.service.ShortUrlExpanderService;

import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CachedSocialServiceImplTest {

	@InjectMocks private CachedSocialServiceImpl<SocialStatus, SocialOwner> socialService;

	@Mock private ShortUrlExpanderService shortUrlExpanderService;

	@BeforeMethod
	private void init() {
		socialService = new CachedSocialServiceImpl<SocialStatus, SocialOwner>() {
			@Override
			public SocialStatus getCurrentStatus(final SocialOwner owner) throws SocialException {
				throw new IllegalStateException("Not implemented in test");
			}
		};

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void expandNoUrls() {
		final String actual = socialService.expandShortUrls("666", ImmutableList.of(Pattern.compile("https:\\/\\/t\\.co\\/\\w+")));
		final String expected = "666";

		verify(shortUrlExpanderService, never()).expandShortUrl(anyString());

		assertEquals(actual, expected);
	}

	@Test
	public void expandShortUrl() {
		final String expectedUrl = "https://www.youtube.com/watch?v=Zi_TMwC_WyI&feature=youtu.be";
		when(shortUrlExpanderService.expandShortUrl(anyString())).thenReturn(expectedUrl);

		final String actual = socialService.expandShortUrls("666 https://t.co/pIqtyHjKVB", ImmutableList.of(Pattern.compile("https:\\/\\/t\\.co\\/\\w+")));
		final String expected = "666 " + expectedUrl;

		verify(shortUrlExpanderService).expandShortUrl(eq("https://t.co/pIqtyHjKVB"));

		assertEquals(actual, expected);
	}

	@Test
	public void expandShortUrls() {
		final String expectedUrl1 = "https://www.youtube.com/watch?v=Zi_TMwC_WyI&feature=youtu.be";
		when(shortUrlExpanderService.expandShortUrl(eq("https://t.co/pIqtyHjKVB"))).thenReturn(expectedUrl1);

		final String expectedUrl2 = "https://steamcommunity.com/sharedfiles/filedetails/?id=702023400";
		when(shortUrlExpanderService.expandShortUrl(eq("https://goo.gl/lH0xwc"))).thenReturn(expectedUrl2);

		final String actual = socialService.expandShortUrls("666 https://t.co/pIqtyHjKVB https://goo.gl/lH0xwc", ImmutableList.of(
		    Pattern.compile("https:\\/\\/t\\.co\\/\\w+"),
		    Pattern.compile("https:\\/\\/goo\\.gl\\/[\\w\\d]+")));
		final String expected = "666 " + expectedUrl1 + " " + expectedUrl2;

		verify(shortUrlExpanderService).expandShortUrl(eq("https://t.co/pIqtyHjKVB"));
		verify(shortUrlExpanderService).expandShortUrl(eq("https://goo.gl/lH0xwc"));

		assertEquals(actual, expected);
	}
}
