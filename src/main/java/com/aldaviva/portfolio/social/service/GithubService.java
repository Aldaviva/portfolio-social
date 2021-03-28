package com.aldaviva.portfolio.social.service;

import com.aldaviva.portfolio.social.data.GithubOwner;
import com.aldaviva.portfolio.social.data.GithubStatus;
import com.aldaviva.portfolio.social.service.GithubService.GithubCacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CacheIndicators;
import com.aldaviva.portfolio.social.service.cache.CachedSocialService;

public interface GithubService extends CachedSocialService<GithubStatus, GithubOwner, GithubCacheIndicators> {

	public class GithubCacheIndicators implements CacheIndicators {

		private HttpCacheIndicators listRepos = new HttpCacheIndicators();
		private HttpCacheIndicators listRepoCommits = new HttpCacheIndicators();

		public HttpCacheIndicators getListRepos() {
			return listRepos;
		}

		public void setListRepos(final HttpCacheIndicators listRepos) {
			this.listRepos = listRepos;
		}

		public HttpCacheIndicators getListRepoCommits() {
			return listRepoCommits;
		}

		public void setListRepoCommits(final HttpCacheIndicators listRepoCommits) {
			this.listRepoCommits = listRepoCommits;
		}

	}
}
