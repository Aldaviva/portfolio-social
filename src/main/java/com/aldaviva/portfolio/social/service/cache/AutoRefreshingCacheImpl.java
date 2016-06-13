package com.aldaviva.portfolio.social.service.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class AutoRefreshingCacheImpl implements AutoRefreshingCache {

	@Autowired private TaskScheduler taskScheduler;

	public Map<String, CacheEntry<?>> cache = new ConcurrentHashMap<>();

	@Override
	public <T> T get(final String key, final ValueGetter<T> valueGetter, final long millisBetweenUpdates) {
		@SuppressWarnings("unchecked")
		CacheEntry<T> entry = (CacheEntry<T>) cache.get(key);
		if(entry == null) {
			entry = new CacheEntry<>();
			entry.valueGetter = valueGetter;
			entry.updateValue();
			if(millisBetweenUpdates > 0) {
				entry.canceller = taskScheduler.scheduleWithFixedDelay(new UpdateTask(entry),
				    new DateTime().plus(millisBetweenUpdates).toDate(), millisBetweenUpdates);
			}
			cache.put(key, entry);
		}

		return entry.mostRecentValue;
	}

	public boolean containskey(final String key) {
		return cache.containsKey(key);
	}

	public void remove(final String key) {
		final CacheEntry<?> oldEntry = cache.remove(key);
		if(oldEntry != null) {
			oldEntry.canceller.cancel(false);
		}
	}

	private static final class CacheEntry<T> {
		public T mostRecentValue;
		public ValueGetter<T> valueGetter;
		public ScheduledFuture<?> canceller;

		public void updateValue() {
			mostRecentValue = valueGetter.getValue();
		}
	}

	private static final class UpdateTask implements Runnable {
		private final CacheEntry<?> entry;

		public UpdateTask(final CacheEntry<?> entry) {
			this.entry = entry;
		}

		@Override
		public void run() {
			entry.updateValue();
		}
	}
}
