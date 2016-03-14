package com.swissquote.foundation.soa.idempotence.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.swissquote.foundation.soa.idempotence.server.JsonUtils;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;
import com.swissquote.foundation.soa.support.api.exceptions.DetailedWebApplicationException;
import com.swissquote.foundation.soa.support.api.exceptions.ErrorDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class TestJsonUtils {
	private JsonUtils jsonUtils;

	@Before
	public void before() {
		this.jsonUtils = new JsonUtils();
	}

	@Test
	public void runtimeException() throws ClassNotFoundException {
		RuntimeException in = new RuntimeException("SimulatedException");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
		ErrorDetails entity = (ErrorDetails) out.getResponse().getEntity();
		Assert.assertEquals(JsonUtils.DEFAULT_ERROR_CODE, entity.getCode());
	}

	@Test
	public void businessCheckedException() throws ClassNotFoundException {
		BusinessCheckedException in = new BusinessCheckedException("BusinessCheckedException");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void extendingBusinessCheckedException() throws ClassNotFoundException {
		MyBusinessCheckedException in = new MyBusinessCheckedException("MyBusinessCheckedException", "tkel9856");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void businessUncheckedException() throws ClassNotFoundException {
		BusinessUncheckedException in = new BusinessUncheckedException("BusinessUncheckedException");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void extendingBusinessUncheckedException() throws ClassNotFoundException {
		MyBusinessUncheckedException in = new MyBusinessUncheckedException("MyBusinessUncheckedException", "test");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void clientException() throws ClassNotFoundException {
		ClientException in = new ClientException("ClientException");
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void detailedWebApplicationException() throws ClassNotFoundException {
		String errorCode = "ERROR_CODE";
		Map<String, String> details = new HashMap<>();
		details.put("key1", "value1");
		DetailedWebApplicationException in =
				new DetailedWebApplicationException("Detailed.Exception", Response.Status.NOT_FOUND.getStatusCode(), errorCode, details);
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());

		ErrorDetails entity = (ErrorDetails) out.getResponse().getEntity();
		Assert.assertEquals(errorCode, entity.getCode());
		Assert.assertEquals(details, entity.getDetails());
	}

	@Test
	public void webApplicationException() throws ClassNotFoundException {
		WebApplicationException in = new WebApplicationException("Webapp.Exception", Response.Status.NOT_FOUND.getStatusCode());
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void customWebApplicationException() throws ClassNotFoundException {
		String value = "Some value";
		WebApplicationException in = new CustomWebApplicationException("CustomWebapp.Exception", value);
		String json = jsonUtils.exceptionToJson(in);
		WebApplicationException out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(in.getMessage(), out.getMessage());
		Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), out.getResponse().getStatus());

		CustomEntity entity = (CustomEntity) out.getResponse().getEntity();
		Assert.assertEquals(value, entity.getValue());
	}

	public static class MyBusinessCheckedException extends BusinessCheckedException {
		private static final long serialVersionUID = -8545160887992869008L;
		private String userId;

		public MyBusinessCheckedException(String message, String userid) {
			super(message);
			this.userId = userid;
		}

		public String getUserId() {
			return userId;
		}

	}

	public static class MyBusinessUncheckedException extends BusinessUncheckedException {
		private static final long serialVersionUID = -8545160887992869008L;
		private String userId;

		public MyBusinessUncheckedException(String message, String userid) {
			super(message);
			this.userId = userid;
		}

		public String getUserId() {
			return userId;
		}

	}

	@RequiredArgsConstructor
	private static class CustomEntity {
		@Getter
		private final String value;
	}

	private class CustomWebApplicationException extends WebApplicationException {
		public CustomWebApplicationException(String message, String value) {
			super(message, Response.status(Response.Status.UNAUTHORIZED).entity(new CustomEntity(value)).build());
		}
	}
}
