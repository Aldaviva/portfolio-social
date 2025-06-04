package com.aldaviva.portfolio.social.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Transparently support Endpoint authentication that requires a separate request to set a session cookie.
 * 
 * These filters can be added to the request chain by the {@link EndpointTargetFactoryImpl} so dispatchers don't have to be concerned about 
 * auth.
 * 
 * When a request fails because no session cookie was sent, this filter will
 *   1. pause the response
 *   2. request a session ID using {@link #startSession(ClientRequestContext)}
 *   3. store the session ID in the Apache client's cookie store
 *   4. replay the original request with the new cookie
 *   5. return the new response to the invoker of the original request
 *   
 * @see org.glassfish.jersey.client.authentication.HttpAuthenticationFilter
 */
public abstract class ReplayingAuthenticationFilter implements ClientResponseFilter, ClientRequestFilter {

	private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ReplayingAuthenticationFilter.class);

	private static final String LOOPING_PROPERTY = "vc.bjn.catalyst.repeat_count";
	private static final int MAX_REPEAT = 20;

	//	protected final String username;
	//	protected final String password;
	//
	//	public ReplayingAuthenticationFilter(final String username, final String password) {
	//		this.username = username;
	//		this.password = password;
	//	}

	/**
	 * If this returns <code>false</code>, the filter will replay the request after calling {@link #startSession(ClientRequestContext)}.
	 * Otherwise, the request will pass through untouched.
	 */
	public boolean isResponseAuthorized(final ClientResponseContext response) {
		final int status = response.getStatus();
		return status != Status.FORBIDDEN.getStatusCode() && status != Status.UNAUTHORIZED.getStatusCode();
	}

	/**
	 * Send a new, intermediate request to generate a session ID based on the username and password.
	 * 
	 * The session ID is assumed by {@link #repeatRequest(ClientRequestContext, ClientResponseContext, String)} to be stored in a 
	 * cookie using the Set-Cookie header as part of the intermediate response, so <code>repeatRequest</code> does not explicitly use 
	 * the session ID by default.
	 * 
	 * @return the generated session ID, or null if session generation failed.
	 */
	public abstract String startSession(ClientRequestContext request);

	@Override
	public void filter(final ClientRequestContext request) throws IOException {
		if (!isRequestAuthorized(request)) {
			startSession(request);
		}
	}

	protected abstract boolean isRequestAuthorized(ClientRequestContext request);

	/**
	 * If you override filter() in a subclass, be sure to check on the isInterrupted() state of the calling thread.
	 * 
	 * Once a thread goes into an interrupted state, it stays that way until Thread.interrupted() is called, and a
	 * thread in an interrupted state will immediately fail on all kinds of different IO.
	 * 
	 * The safest thing to do is just bail on the request with an IOException in the event that the calling thread
	 * is in an interrupted state, because anything downstream from here (especially when requests are going to be
	 * repeated) is fairly likely to fail.
	 */
	@Override
	public void filter(final ClientRequestContext request, final ClientResponseContext response) throws IOException {
		checkInterrupted();

		if (!isResponseAuthorized(response)) {
			final String sessionId = startSession(request);
			if (sessionId != null) {
				// Closing the original response stream used to happen whether or not we successfully started a session, but that 
				// caused issues when auth failed and we let the dispatcher try to read from a closed stream.
				// Hopefully we can close the original response stream after calling startSession() without causing any issues.
				try {
					response.getEntityStream().close();
				} catch (final IOException e) {
				}
				repeatRequest(request, response, sessionId);
			}
		}
	}

	protected final void checkInterrupted() throws IOException {
		if (Thread.currentThread().isInterrupted()) { // calling isInterrupted() doesn't clear the interrupted state. 
			LOGGER.info("Thread {} is in an interrupted state. Bailing on this request.", Thread.currentThread().getName());
			throw new IOException("Interrupted");
		}
	}

	/**
	 * Copied from {@link org.glassfish.jersey.client.authentication.HttpAuthenticationFilter#repeatRequest(ClientRequestContext, ClientResponseContext, String)}
	 * @param sessionId 
	 * @throws IOException if the request is repeated too many times
	 */
	protected final void repeatRequest(final ClientRequestContext request, final ClientResponseContext response, final String sessionId) throws IOException {
		final String method = request.getMethod();
		final WebTarget resourceTarget = getReplayTarget(request, sessionId);

		// Changed from HttpAuthenticationFilter because they were copying the Content-Type header value to the Accept header,
		// which I think is a bug in JAX-RS.
		final Invocation.Builder builder = resourceTarget.request(request.getAcceptableMediaTypes().toArray(new MediaType[0]));

		final MultivaluedMap<String, Object> originalHeaders = request.getHeaders();
		originalHeaders.remove(HttpHeaders.COOKIE);
		builder.headers(originalHeaders);

		// Try to keep from ever getting into a request repeat loop by limiting how many times
		// we'll ever work on the same original request.
		Integer requestCount = (Integer) request.getProperty(LOOPING_PROPERTY);
		if (requestCount == null) {
			requestCount = Integer.valueOf(1);
		} else {
			requestCount = Integer.valueOf(requestCount + 1);
		}
		if (requestCount >= MAX_REPEAT) {
			throw new IOException("Request retry limit exceeded");
		}
		builder.property(LOOPING_PROPERTY, requestCount);

		Invocation invocation;
		if (request.getEntity() == null) {
			invocation = builder.build(method);
		} else {
			invocation = builder.build(method, Entity.entity(request.getEntity(), request.getMediaType()));
		}
		final Response nextResponse = invocation.invoke();

		// always set the response entity stream from the repeated request because hasEntity returns false for Content-Length: 0
		response.setEntityStream(nextResponse.readEntity(InputStream.class));

		final MultivaluedMap<String, String> headers = response.getHeaders();
		headers.clear();
		headers.putAll(nextResponse.getStringHeaders());
		response.setStatus(nextResponse.getStatus());
	}

	//	protected URI getReplayUri(final URI originalRequestUri, final String sessionId) {
	//		LOGGER.trace("now that we have started session {}, replaying request to {}", sessionId, originalRequestUri);
	//		return originalRequestUri;
	//	}

	protected WebTarget getReplayTarget(final ClientRequestContext request, final String sessionId) {
		final URI originalRequestUri = request.getUri();
		LOGGER.trace("now that we have started session {}, replaying request to {}", sessionId, originalRequestUri);
		return request.getClient().target(originalRequestUri);
	}

}