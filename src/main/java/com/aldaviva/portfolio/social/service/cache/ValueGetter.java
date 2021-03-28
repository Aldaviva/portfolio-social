package com.aldaviva.portfolio.social.service.cache;

import com.aldaviva.portfolio.social.service.cache.CacheIndicators.None;

public interface ValueGetter<T, CACHE extends CacheIndicators> {

	ValueGetterResult<T, CACHE> getValue(CACHE cacheIndicators);

	public interface ValueGetterResult<T, CACHE> {

		T getValue();

		CACHE getCacheIndicators();
	}

	public class NonCachingResult<T> implements ValueGetterResult<T, CacheIndicators.None> {

		private final T value;

		public NonCachingResult(final T value) {
			this.value = value;
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public None getCacheIndicators() {
			return null;
		}

	}

	public class CachedHttpResult<T, C extends CacheIndicators> implements ValueGetterResult<T, C> {
		private T value;
		private C cacheIndicators;

		public CachedHttpResult(final T result, final C cacheIndicators) {
			super();
			this.value = result;
			this.cacheIndicators = cacheIndicators;
		}

		@Override
		public T getValue() {
			return value;
		}

		public void setValue(final T value) {
			this.value = value;
		}

		@Override
		public C getCacheIndicators() {
			return cacheIndicators;
		}

		public void setCacheIndicators(final C cacheIndicators) {
			this.cacheIndicators = cacheIndicators;
		}

	}

	public class Unmodified<T, C extends CacheIndicators> implements ValueGetterResult<T, C> {

		@Override
		public T getValue() {
			return null;
		}

		@Override
		public C getCacheIndicators() {
			return null;
		}

	}
}
