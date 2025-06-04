package com.aldaviva.portfolio.social.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BlueskyAuthenticationFilter extends ReplayingAuthenticationFilter {

	private static final String SKIP_PREAUTHENTICATION = "com.aldaviva.portfolio.social.skip_preauthentication";
	private static final Duration ACCESS_TOKEN_VALIDITY = Duration.standardHours(2); // decoded from accessJwt[iat, exp] on 2025-06-03
	private static final Duration REFRESH_TOKEN_VALIDITY = Duration.standardDays(90);
	private static final Duration SAFETY_FACTOR = Duration.standardMinutes(5);

	@Value("${bluesky.auth.handleoremailaddress}") private String handleOrEmailAddress;
	@Value("${bluesky.auth.password}") private String appPassword;

	private String accessToken;
	private String refreshToken;
	private DateTime accessTokenCreationTime;
	private DateTime refreshTokenCreationTime;

	/**
	 * Sign in with a handle/email address and password/app password.
	 * After this method returns, subsequent requests made by this object instance will automatically use the received access token.
	 * To sign out, call {@link #signOut()}.
	 * @return the session ID (which happens to be a JSON Web Token)
	 * @see https://docs.bsky.app/docs/api/com-atproto-server-create-session
	 * @see https://github.com/bluesky-social/atproto/blob/main/lexicons/com/atproto/server/createSession.json
	 * @see https://docs.bsky.app/docs/api/com-atproto-server-refresh-session
	 * @see https://github.com/bluesky-social/atproto/blob/main/lexicons/com/atproto/server/refreshSession.json
	 */
	@Override
	public String startSession(final ClientRequestContext request) {
		ObjectNode response = null;

		if (isTokenValid(true)) {
			try {
				response = request.getClient()
				    .target(BlueskyClientImpl.API_BASE)
				    .property(SKIP_PREAUTHENTICATION, Boolean.TRUE)
				    .path("com.atproto.server.refreshSession")
				    .request()
				    .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken)
				    .post(null, ObjectNode.class);
			} catch (final WebApplicationException | ProcessingException e) {
			}
		}

		if (response == null) {
			final Map<String, String> authenticationRequestBody = new HashMap<>();
			authenticationRequestBody.put("identifier", handleOrEmailAddress);
			authenticationRequestBody.put("password", appPassword);

			try {
				response = request.getClient()
				    .target(BlueskyClientImpl.API_BASE)
				    .property(SKIP_PREAUTHENTICATION, Boolean.TRUE)
				    .path("com.atproto.server.createSession")
				    .request()
				    .post(Entity.json(authenticationRequestBody), ObjectNode.class);

				refreshToken = response.get("refreshJwt").textValue();
				refreshTokenCreationTime = new DateTime();
			} catch (final NotAuthorizedException e) {
			}
		}

		if (response != null) {
			accessToken = response.get("accessJwt").textValue();
			accessTokenCreationTime = DateTime.now();
		} else {
			signOut();
		}

		return accessToken;
	}

	public void signOut() {
		accessToken = null;
		accessTokenCreationTime = null;
		refreshToken = null;
		refreshTokenCreationTime = null;
	}

	@Override
	public void filter(final ClientRequestContext request) throws IOException {
		super.filter(request);

		if (accessToken != null) {
			request.getHeaders().putIfAbsent(HttpHeaders.AUTHORIZATION, Arrays.asList("Bearer " + accessToken));
		}
	}

	@Override
	protected boolean isRequestAuthorized(final ClientRequestContext request) {
		return isTokenValid(false) || request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION) != null || Boolean.TRUE.equals(request.getProperty(SKIP_PREAUTHENTICATION));
	}

	private boolean isTokenValid(final boolean isRefreshToken) {
		final String token = isRefreshToken ? refreshToken : accessToken;
		final DateTime creationTime = isRefreshToken ? refreshTokenCreationTime : accessTokenCreationTime;
		final Duration maximumValidity = (isRefreshToken ? REFRESH_TOKEN_VALIDITY : ACCESS_TOKEN_VALIDITY).minus(SAFETY_FACTOR);
		return token != null && creationTime != null && new Duration(creationTime, null).isShorterThan(maximumValidity);
	}

}
