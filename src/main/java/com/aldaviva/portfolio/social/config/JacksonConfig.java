package com.aldaviva.portfolio.social.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.glassfish.jersey.CommonProperties;
import org.springframework.beans.factory.annotation.Autowired;

@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

	@Autowired private ObjectMapper objectMapper;

	@Override
	public ObjectMapper getContext(final Class<?> type) {
		return objectMapper;
	}

	/** New version of {@link org.glassfish.jersey.jackson.JacksonFeature JacksonFeature} for Jackson 2. */
	public static final class Jackson2Feature implements Feature {
		@Override
		public boolean configure(final FeatureContext context) {
			final String disableMoxy = CommonProperties.MOXY_JSON_FEATURE_DISABLE + '.'
			    + context.getConfiguration().getRuntimeType().name().toLowerCase();
			context.property(disableMoxy, true);

			context.register(JacksonJaxbJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
			return true;
		}
	}

}
