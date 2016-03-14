package com.swissquote.foundation.soa.idempotence.server;

import static javax.ws.rs.core.Response.Status.fromStatusCode;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.swissquote.foundation.soa.gson.GsonConfig;
import com.swissquote.foundation.soa.support.api.exceptions.DetailedWebApplicationException;

import lombok.Data;

public class JsonUtils {
	public static final String DEFAULT_ERROR_CODE = "DEFAULT_ERROR_CODE";

	private final Gson gson = GsonConfig.buildSpecificConfig();

	public <T> String toJson(T processingResponse) {
		return gson.toJson(processingResponse);
	}

	public <T> T fromJson(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public String exceptionToJson(final Exception exception) {
		WebApplicationException webAppException = mapException(exception);

		return mappedExceptionToJson(webAppException);
	}

	public String mappedExceptionToJson(WebApplicationException webAppException) {
		JsonRepresentationContainer holder = new JsonRepresentationContainer();
		holder.setStatus(webAppException.getResponse().getStatus());
		holder.setMessage(webAppException.getMessage());
		if (webAppException.getResponse().hasEntity()) {
			holder.setEntityClassName(webAppException.getResponse().getEntity().getClass().getName());
			holder.setEntityJson(toJson(webAppException.getResponse().getEntity()));
		}

		return toJson(holder);
	}

	public WebApplicationException mapException(Exception exception) {
		if (exception instanceof WebApplicationException) {
			return (WebApplicationException) exception;
		}

		return new DetailedWebApplicationException(exception.getMessage(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
				DEFAULT_ERROR_CODE);
	}

	public WebApplicationException exceptionFromJson(final String json) throws ClassNotFoundException {
		JsonRepresentationContainer response = fromJson(json, JsonRepresentationContainer.class);
		String className = response.getEntityClassName();

		if (className == null) {
			return new WebApplicationException(response.getMessage(), response.getStatus());
		}

		Class<?> exceptionClass = Class.forName(className);
		Object entity = fromJson(response.getEntityJson(), exceptionClass);

		return new WebApplicationException(response.getMessage(), Response.status(fromStatusCode(response.getStatus())).entity(entity).build());
	}

	@Data
	private static class JsonRepresentationContainer {
		private String entityClassName;
		private String entityJson;
		private String message;
		private int status;
	}
}