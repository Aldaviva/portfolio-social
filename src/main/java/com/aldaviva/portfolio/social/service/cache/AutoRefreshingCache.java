package com.aldaviva.portfolio.social.service.cache;

public interface AutoRefreshingCache<CACHE extends CacheIndicators> {

	public <T> T get(String key, ValueGetter<T, CACHE> valueGetter, long millisBetweenUpdates);

}
