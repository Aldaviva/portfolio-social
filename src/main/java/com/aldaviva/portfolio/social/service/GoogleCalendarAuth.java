package com.aldaviva.portfolio.social.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleCalendarAuth {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HttpTransport.class);
	private static final String ACCOUNT_ID = "966028723158-goc7b14ff39duk3a0t54nrha43qtlr5q@developer.gserviceaccount.com";
	private static final String KEY_FILENAME = "/META-INF/calendar_key.p12";
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static final NetHttpTransport NET_HTTP_TRANSPORT = new NetHttpTransport();

	public GoogleCalendarAuth() {
		enableHttpLogging();
	}

	@Bean
	public Calendar getCalendar() throws GeneralSecurityException, IOException, URISyntaxException {
		return new Calendar.Builder(NET_HTTP_TRANSPORT, JSON_FACTORY, getServiceCredentials()).build();
	}

	private GoogleCredential getServiceCredentials() throws GeneralSecurityException, IOException, URISyntaxException {
		return new GoogleCredential.Builder()
		    .setTransport(NET_HTTP_TRANSPORT)
		    .setJsonFactory(JSON_FACTORY)
		    .setServiceAccountId(ACCOUNT_ID)
		    .setServiceAccountScopes(Collections.singletonList(CalendarScopes.CALENDAR_READONLY))
		    .setServiceAccountPrivateKeyFromP12File(new File(getClass().getResource(KEY_FILENAME).toURI()))
		    .build();
	}

	private static void enableHttpLogging() {
		SLF4JBridgeHandler.install();

		final Logger logger = Logger.getLogger(HttpTransport.class.getName());
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(new Handler() {
			@Override
			public void publish(final LogRecord record) {
				if (record.getLevel().intValue() < Level.INFO.intValue()) {
					LOGGER.trace(record.getMessage());
				}
			}

			@Override
			public void close() throws SecurityException {
			}

			@Override
			public void flush() {
			}
		});
	}

}