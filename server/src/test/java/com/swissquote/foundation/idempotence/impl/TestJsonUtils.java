package com.swissquote.foundation.idempotence.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.swissquote.foundation.soa.idempotence.server.impl.JsonUtils;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

public class TestJsonUtils {
	private JsonUtils jsonUtils;

	@Before
	public void before() {
		this.jsonUtils = new JsonUtils();
	}

	@Test
	public void runtimeException() {
		RuntimeException in = new RuntimeException("SimulatedException");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(BusinessUncheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void businessCheckedException() {
		BusinessCheckedException in = new BusinessCheckedException("BusinessCheckedException");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(BusinessCheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void extendingBusinessCheckedException() {
		MyBusinessCheckedException in = new MyBusinessCheckedException("MyBusinessCheckedException", "tkel9856");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(MyBusinessCheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());

	}

	@Test
	public void twoLavelsOfExtensionFromBusinessCheckedException() {
		MySecondBusinessCheckedException in =
				new MySecondBusinessCheckedException("MySecondBusinessCheckedException", "tkel9856", Integer.valueOf(12));
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(MySecondBusinessCheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());

	}

	@Test
	public void businessUncheckedException() {
		BusinessUncheckedException in = new BusinessUncheckedException("BusinessUncheckedException");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(BusinessUncheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void extendingBusinessUncheckedException() {
		MyBusinessUncheckedException in = new MyBusinessUncheckedException("MyBusinessUncheckedException", "test");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(MyBusinessUncheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void twoLavelsOfExtentionForBusinessUncheckedException() {
		MySecondBusinessUncheckedException in = new MySecondBusinessUncheckedException("MySecondBusinessUncheckedException", "test", 13);
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(MySecondBusinessUncheckedException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
	}

	@Test
	public void clientException() {
		ClientException in = new ClientException("ClientException");
		String json = jsonUtils.exceptionToJson(in);
		Exception out = jsonUtils.exceptionFromJson(json);
		Assert.assertNotNull(out);
		Assert.assertEquals(ClientException.class, out.getClass());
		Assert.assertEquals(in.getMessage(), out.getMessage());
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

		public void setUserId(String userId) {
			this.userId = userId;
		}

	}

	public static class MySecondBusinessCheckedException extends MyBusinessCheckedException {
		private static final long serialVersionUID = -8545160887992869008L;
		private Integer value;

		public MySecondBusinessCheckedException(String message, String userid, Integer value) {
			super(message, userid);
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
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

		public void setUserId(String userId) {
			this.userId = userId;
		}

	}

	public static class MySecondBusinessUncheckedException extends MyBusinessUncheckedException {
		private static final long serialVersionUID = -8545160887992869008L;
		private Integer value;

		public MySecondBusinessUncheckedException(String message, String userid, Integer value) {
			super(message, userid);
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}

		public void setValue(Integer value) {
			this.value = value;
		}
	}
}
