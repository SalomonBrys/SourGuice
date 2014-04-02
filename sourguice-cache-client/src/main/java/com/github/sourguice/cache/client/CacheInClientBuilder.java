package com.github.sourguice.cache.client;

import java.lang.annotation.Annotation;

import com.github.sourguice.value.ValueConstants;

@SuppressWarnings({ "javadoc", "PMD.MethodNamingConventions", "PMD.CyclomaticComplexity", "PMD.TooManyMethods" })
public class CacheInClientBuilder {

	protected boolean flagPublic = true;
	protected boolean flagNoTransform = false;
	protected boolean flagNoStore = false;
	protected boolean flagMustRevalidate = false;
	protected String valuePrivate = ValueConstants.DEFAULT_NONE;
	protected String valueNoCache = ValueConstants.DEFAULT_NONE;
	protected int valueMaxAge = -1;
	protected int valueSMaxAge = -1;
	protected String valueExtension = "";

	public CacheInClientBuilder Public(final boolean flag) {
		this.flagPublic = flag;
		return this;
	}

	public CacheInClientBuilder NoTransform(final boolean flag) {
		this.flagNoTransform = flag;
		return this;
	}

	public CacheInClientBuilder NoStore(final boolean flag) {
		this.flagNoStore = flag;
		return this;
	}

	public CacheInClientBuilder MustRevalidate(final boolean flag) {
		this.flagMustRevalidate = flag;
		return this;
	}

	public CacheInClientBuilder Private(final String value) {
		this.valuePrivate = value;
		return this;
	}

	public CacheInClientBuilder Private(final boolean flag) {
		return Private(flag ? "" : ValueConstants.DEFAULT_NONE);
	}

	public CacheInClientBuilder NoCache(final String value) {
		this.valueNoCache = value;
		return this;
	}

	public CacheInClientBuilder NoCache(final boolean flag) {
		return NoCache(flag ? "" : ValueConstants.DEFAULT_NONE);
	}

	public CacheInClientBuilder MaxAge(final int value) {
		this.valueMaxAge = value;
		return this;
	}

	public CacheInClientBuilder SMaxAge(final int value) {
		this.valueSMaxAge = value;
		return this;
	}

	public CacheInClientBuilder Extension(final String value) {
		this.valueExtension = value;
		return this;
	}


	public CacheInClient build() {
		return new CacheInClient() {
			@Override public Class<? extends Annotation> annotationType() { return CacheInClient.class; }
			@Override public boolean Public() { return CacheInClientBuilder.this.flagPublic; }
			@Override public boolean NoTransform() { return CacheInClientBuilder.this.flagNoTransform; }
			@Override public boolean NoStore() { return CacheInClientBuilder.this.flagNoStore; }
			@Override public boolean MustRevalidate() { return CacheInClientBuilder.this.flagMustRevalidate; }
			@Override public String Private() { return CacheInClientBuilder.this.valuePrivate; }
			@Override public String NoCache() { return CacheInClientBuilder.this.valueNoCache; }
			@Override public int MaxAge() { return CacheInClientBuilder.this.valueMaxAge; }
			@Override public int SMaxAge() { return CacheInClientBuilder.this.valueSMaxAge; }
			@Override public String Extension() { return CacheInClientBuilder.this.valueExtension; }
		};
	}
}