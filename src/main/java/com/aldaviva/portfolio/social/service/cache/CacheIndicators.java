package com.aldaviva.portfolio.social.service.cache;

import java.util.Date;
import java.util.Locale;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Response;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public interface CacheIndicators {

	interface None extends CacheIndicators {
	}

	public class HttpCacheIndicators implements CacheIndicators {
		public static final DateTimeFormatter HTTP_DATETIME_FORMATTER = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss z")
		    .withLocale(Locale.US).withZone(DateTimeZone.forID("GMT"));

		private String eTag;
		private DateTime lastModified;

		public HttpCacheIndicators() {

		}

		public HttpCacheIndicators(final Response httpResponse) {
			this();

			final EntityTag entityTag = httpResponse.getEntityTag();
			if (entityTag != null) {
				setEtag(entityTag.getValue());
			}

			final Date lastModifiedHeader = httpResponse.getLastModified();
			if (lastModifiedHeader != null) {
				setLastModified(lastModifiedHeader);
			}
		}

		public String getEtag() {
			return eTag;
		}

		public void setEtag(final String eTag) {
			this.eTag = eTag;
		}

		public DateTime getLastModified() {
			return lastModified;
		}

		public String getLastModifiedHeader() {
			return lastModified != null ? HTTP_DATETIME_FORMATTER.print(lastModified) : null;
		}

		public void setLastModified(final DateTime lastModified) {
			this.lastModified = lastModified;
		}

		public void setLastModified(final Date lastModified) {
			this.lastModified = lastModified != null ? new DateTime(lastModified) : null;
		}

	}
}
