package com.swissquote.foundation.soa.idempotence.server.impl;

import java.util.HashSet;
import java.util.Set;

import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;
import com.swissquote.foundation.soa.support.platform.gson.GsonConfig;

public class JsonUtils {
	private final GsonConfig gsonConfig = new GsonConfig();

	public <T> String toJson(T processingResponse) {
		return gsonConfig.getGson().toJson(processingResponse);
	}

	public <T> T fromJson(String json, Class<T> classOfT) {
		return gsonConfig.getGson().fromJson(json, classOfT);
	}

	public String exceptionToJson(final Exception exception) {
		Exception toSerialize;
		if (canBeSerialisedDirectly(exception.getClass())) {
			toSerialize = exception;
		} else {
			toSerialize = new BusinessUncheckedException(exception.getMessage());
		}

		String gsonRepresentation = gsonConfig.getGson().toJson(toSerialize);

		JsonRepresentationContainer holder = new JsonRepresentationContainer();
		holder.setClassName(toSerialize.getClass().getName());
		holder.setGsonRepresentation(gsonRepresentation);
		return gsonConfig.getGson().toJson(holder);
	}

	public Exception exceptionFromJson(final String json) {
		JsonRepresentationContainer response = gsonConfig.getGson().fromJson(json, JsonRepresentationContainer.class);
		String className = response.getClassName();
		try {

			Class<?> exceptionClass = Class.forName(className);
			return (Exception) gsonConfig.getGson().fromJson(response.getGsonRepresentation(), exceptionClass);
		}
		catch (ClassNotFoundException e) {
			throw new BusinessUncheckedException("Class not found: " + className);
		}
	}

	private static final Set<Class<? extends Exception>> soaExceptionsClasses = new HashSet<Class<? extends Exception>>() {
		{
			add(BusinessCheckedException.class);
			add(BusinessUncheckedException.class);
			add(ClientException.class);
		}
	};

	private boolean canBeSerialisedDirectly(final Class<?> clazz) {
		if (soaExceptionsClasses.contains(clazz)) {
			return true;
		}

		if (clazz.getSuperclass() == null) {
			return false;
		}

		return canBeSerialisedDirectly(clazz.getSuperclass());
	}

	public static class JsonRepresentationContainer {
		private String className;
		private String gsonRepresentation;

		public String getClassName() {
			return className;
		}

		public void setClassName(final String className) {
			this.className = className;
		}

		public String getGsonRepresentation() {
			return gsonRepresentation;
		}

		public void setGsonRepresentation(final String gsonRepresentation) {
			this.gsonRepresentation = gsonRepresentation;
		}
	}
}
