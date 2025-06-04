package com.aldaviva.portfolio.social.service;

import java.net.URI;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;

public class BlueskySchema {

	private BlueskySchema() {
	}

	@XmlRootElement
	public static final class PaginatedFeed {
		public List<FeedItem> feed;
		public String cursor;
	}

	public static final class FeedItem {
		public Post post;
	}

	public static final class Post {
		public URI uri;
		public String cid;
		public Author author;
		public Record record;
	}

	public static final class Author {
		public String did;
		public String handle;
		public String displayName;
		public URI avatar;
	}

	public static final class Record {
		public String text;
		public DateTime createdAt;
	}
}
