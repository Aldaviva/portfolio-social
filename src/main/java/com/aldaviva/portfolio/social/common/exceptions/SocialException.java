package com.aldaviva.portfolio.social.common.exceptions;

public class SocialException extends Exception {

	private static final long serialVersionUID = 1L;

	private SocialException(final Exception e) {
		super(e);
	}

	public SocialException(final String message, final Exception e) {
		super(message, e);
	}

	public static class TwitterException extends SocialException {

		private static final long serialVersionUID = 1L;

		public TwitterException(final Exception e) {
			super(e);
		}

		public TwitterException(final String message, final Exception e) {
			super(message, e);
		}
	}

	public static class ThisIsMyJamException extends SocialException {

		private static final long serialVersionUID = 1L;

		public ThisIsMyJamException(final Exception e) {
			super(e);
		}

		public ThisIsMyJamException(final String message, final Exception e) {
			super(message, e);
		}
	}

	public static class FlickrException extends SocialException {

		private static final long serialVersionUID = 1L;

		public FlickrException(final Exception e) {
			super(e);
		}

		public FlickrException(final String message, final Exception e) {
			super(message, e);
		}
	}
}
