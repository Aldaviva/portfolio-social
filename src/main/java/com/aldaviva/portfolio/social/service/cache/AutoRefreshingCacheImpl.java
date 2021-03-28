package com.aldaviva.portfolio.social.service.cache;

import com.aldaviva.portfolio.social.service.cache.ValueGetter.Unmodified;
import com.aldaviva.portfolio.social.service.cache.ValueGetter.ValueGetterResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class AutoRefreshingCacheImpl<CACHE extends CacheIndicators> implements AutoRefreshingCache<CACHE> {

	@Autowired private TaskScheduler taskScheduler;

	public Map<String, CacheEntry<?, ?>> cache = new ConcurrentHashMap<>();

	@Override
	public <T> T get(final String key, final ValueGetter<T, CACHE> valueGetter, final long millisBetweenUpdates) {
		@SuppressWarnings("unchecked")
		CacheEntry<T, CACHE> entry = (CacheEntry<T, CACHE>) cache.get(key);
		if (entry == null) {
			entry = new CacheEntry<>();
			entry.valueGetter = valueGetter;
			entry.updateValue();
			if (millisBetweenUpdates > 0) {
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
		final CacheEntry<?, ?> oldEntry = cache.remove(key);
		if (oldEntry != null) {
			oldEntry.canceller.cancel(false);
		}
	}

	private static final class CacheEntry<T, CACHE extends CacheIndicators> {
		public T mostRecentValue;
		public ValueGetter<T, CACHE> valueGetter;
		public ScheduledFuture<?> canceller;
		public CACHE cacheIndicators;

		public void updateValue() {
			final ValueGetterResult<T, CACHE> result = valueGetter.getValue(cacheIndicators);
			if (!(result instanceof Unmodified)) {
				// the cache missed, save new result
				mostRecentValue = result.getValue();
				cacheIndicators = result.getCacheIndicators();
			}
			// otherwise the cache hit, don't change any saved state
		}
	}

	private static final class UpdateTask implements Runnable {
		private final CacheEntry<?, ?> entry;

		public UpdateTask(final CacheEntry<?, ?> entry) {
			this.entry = entry;
		}

		@Override
		public void run() {
			entry.updateValue();
		}
	}

}
