package com.aldaviva.portfolio.social.service.cache;

public interface AutoRefreshingCache {

	public <T> T get(String key, ValueGetter<T> valueGetter, long millisBetweenUpdates);
}
