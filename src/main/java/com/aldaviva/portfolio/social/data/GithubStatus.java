package com.aldaviva.portfolio.social.data;

import org.joda.time.DateTime;

public class GithubStatus implements SocialStatus {

	private String message;
	private DateTime commitDate;
	private String commitHash;
	private String webDiffUrl;
	private String repoFullName;

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public DateTime getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(final DateTime commitDate) {
		this.commitDate = commitDate;
	}

	public String getCommitHash() {
		return commitHash;
	}

	public void setCommitHash(final String commitHash) {
		this.commitHash = commitHash;
	}

	public String getWebDiffUrl() {
		return webDiffUrl;
	}

	public void setWebDiffUrl(final String webDiffUrl) {
		this.webDiffUrl = webDiffUrl;
	}

	public String getRepoFullName() {
		return repoFullName;
	}

	public void setRepoFullName(final String repoFullName) {
		this.repoFullName = repoFullName;
	}

	@Override
	public String toString() {
		return "GithubStatus [message=" + message + ", commitDate=" + commitDate + ", commitHash=" + commitHash + ", webDiffUrl=" + webDiffUrl
		    + ", repoFullName=" + repoFullName + "]";
	}

}
