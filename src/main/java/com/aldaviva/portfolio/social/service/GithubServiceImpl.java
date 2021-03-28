package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.common.exceptions.SocialException;
import com.aldaviva.portfolio.social.data.GithubOwner;
import com.aldaviva.portfolio.social.data.GithubStatus;
import com.aldaviva.portfolio.social.service.GithubService.GithubCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators.HttpCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialServiceImpl;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.CachedHttpResult;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.Unmodified;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.stereotype.Service;

@Service
public class GithubServiceImpl extends CachedSocialServiceImpl<GithubStatus, GithubOwner, GithubCacheIndicators> implements GithubService {

	private static final URI ROOT_API_URL = URI.create("https://api.github.com/");
	private static final String VERSIONED_MEDIA_TYPE = "application/vnd.github.v3+json";

	@Inject private Client webClient;

	@Override
	public ValueGetterResult<GithubStatus, GithubCacheIndicators> getCurrentStatus(final GithubOwner owner, final GithubCacheIndicators cache)
	    throws SocialException {
		try {
			final GithubCacheIndicators resultCache = new GithubCacheIndicators();
			final GithubStatus status = new GithubStatus();

			// https://docs.github.com/en/rest/reference/repos#list-repositories-for-a-user
			final Response repoListResponse = webClient.target(ROOT_API_URL)
			    .path("users")
			    .path(owner.getUsername())
			    .path("repos")
			    .queryParam("type", "all")
			    .queryParam("sort", "pushed")
			    .queryParam("direction", "desc")
			    .queryParam("per_page", 1)
			    .queryParam("page", 1)
			    .request(VERSIONED_MEDIA_TYPE)
			    .header(HttpHeaders.IF_MODIFIED_SINCE, cache != null ? cache.getListRepos().getLastModifiedHeader() : null)
			    .header(HttpHeaders.ETAG, cache != null ? cache.getListRepos().getEtag() : null)
			    .get(Response.class);

			if (repoListResponse.getStatus() == Status.NOT_MODIFIED.getStatusCode()) {
				repoListResponse.close();
				return new Unmodified<>();
			}

			final JsonNode repoList = repoListResponse.readEntity(JsonNode.class);
			resultCache.setListRepos(new HttpCacheIndicators(repoListResponse));
			repoListResponse.close();

			status.setRepoFullName(repoList.path(0).path("full_name").asText());
			final String commitUrlTemplate = convertRubyUriTemplateToJava(repoList.path(0).path("commits_url").textValue());

			// https://docs.github.com/en/rest/reference/repos#get-a-commit
			final Response commitResponse = webClient.target(UriBuilder.fromUri(commitUrlTemplate))
			    .resolveTemplate("sha", "HEAD")
			    .request(VERSIONED_MEDIA_TYPE)
			    .header(HttpHeaders.IF_MODIFIED_SINCE, cache != null ? cache.getListRepoCommits().getLastModifiedHeader() : null)
			    .header(HttpHeaders.ETAG, cache != null ? cache.getListRepoCommits().getEtag() : null)
			    .get(Response.class);

			if (commitResponse.getStatus() == Status.NOT_MODIFIED.getStatusCode()) {
				commitResponse.close();
				return new Unmodified<>();
			}

			final JsonNode commit = commitResponse.readEntity(JsonNode.class);
			resultCache.setListRepoCommits(new HttpCacheIndicators(commitResponse));
			commitResponse.close();

			status.setCommitDate(ISODateTimeFormat.dateTimeParser().parseDateTime(commit.path("commit").path("committer").path("date").asText()));
			status.setCommitHash(commit.path("sha").textValue());
			status.setMessage(commit.path("commit").path("message").textValue());
			status.setWebDiffUrl(commit.path("html_url").textValue());

			return new CachedHttpResult<>(status, resultCache);

		} catch (ProcessingException | WebApplicationException e) {
			throw new SocialException.FlickrException("Failed to get current photo from Flickr", e);
		}
	}

	private static String convertRubyUriTemplateToJava(final String rubyUriTemplate) {
		String result = rubyUriTemplate
		    .replaceAll("\\{/", "/{")
		    .replaceAll("\\{\\+", "{");

		final Matcher matcher = Pattern.compile("\\{\\?(\\w+(?:,\\w+)*)\\}").matcher(result);
		if (matcher.find()) {
			final StringBuilder stringBuilder = new StringBuilder(), queryParamBuilder = new StringBuilder();
			for (final String queryParamName : matcher.group(1).split(",")) {
				queryParamBuilder.append(queryParamBuilder.length() == 0 ? '?' : '&')
				    .append(queryParamName)
				    .append("={")
				    .append(queryParamName)
				    .append('}');
			}
			matcher.appendReplacement(stringBuilder, queryParamBuilder.toString());
			matcher.appendTail(stringBuilder);
			result = stringBuilder.toString();
		}

		return result;
	}
}
