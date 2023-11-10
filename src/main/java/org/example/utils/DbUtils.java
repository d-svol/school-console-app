package org.example.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Properties;

public final class DbUtils {
	private static final String FILE_CONFIG_NAME = "/db.properties";

	private DbUtils() {
		throw new AssertionError("Not for instantiation: " + getClass());
	}

	public static HikariDataSource createDataSource() {
		Properties properties = loadProperties();
		HikariConfig config = new HikariConfig(properties);
		config.setMaximumPoolSize(10);
		return new HikariDataSource(config);
	}

	private static Properties loadProperties() {
		try {
			Properties properties = new Properties();
			properties.load(DbUtils.class.getResourceAsStream(FILE_CONFIG_NAME));
			return properties;
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load db.properties: ", e);
		}
	}
}

