package com.movieapp.backend.tmdb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tmdb.api")
public class TmdbProperties {

	private String key;
	private String baseUrl = "https://api.themoviedb.org";

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key == null ? null : key.trim();
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl == null ? null : baseUrl.trim();
	}
}
