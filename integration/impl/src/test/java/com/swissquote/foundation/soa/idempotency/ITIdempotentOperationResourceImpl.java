package com.swissquote.foundation.soa.idempotency;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Strings;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-applicationContext.xml"})
public class ITIdempotentOperationResourceImpl {
	private static final String SEPARATOR = Strings.repeat("><", 25);

	private static final Logger LOGGER = LoggerFactory.getLogger(ITIdempotentOperationResourceImpl.class);

	@Autowired
	private IdempotentOperationResource resource;

	@Test
	public void theServiceCanCreateANewOperation() {
		Long operationId = resource.createNewOperation();
		Assert.assertNotNull(operationId);
		LOGGER.info("New Operation created with id " + operationId);
	}

	@Test
	public void throwingABusinessCheckedException() {
		// a simple operation that throws an exception
		Operation operation1 =
				new Operation().setThrowBusinessCheckedExcetion(true).setDescription("BusinessCheckedException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		Assert.assertNotNull(t1);
		log("BusinessCheckedException exception from a simple call", t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
				.setThrowBusinessCheckedExcetion(true)
				.setDescription("BusinessCheckedException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);
		log("BusinessCheckedException exception from an idempotent call", t2);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		log("BusinessCheckedException exception from a repeated idempotent call", t3);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingABusinessUncheckedException() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
		.setThrowBusinessUncheckedExcetion(true)
		.setDescription("BusinessUncheckedException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		log("BusinessUncheckedException exception from a simple call", t1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
		.setThrowBusinessUncheckedExcetion(true)
		.setDescription("BusinessUncheckedException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		log("BusinessUncheckedException exception from an idempotent call", t2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		log("BusinessUncheckedException exception from a repeated idempotent call", t3);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingAClientException() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
		.setThrowClientException(true)
		.setDescription("ClientException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		log("ClientException exception from a simple call", t1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
		.setThrowClientException(true)
		.setDescription("ClientException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		log("ClientException exception from an idempotent call", t2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		log("ClientException exception from a repeated idempotent call", t3);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingGenericThrowable() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
		.setThrowGenericThrowable(true)
		.setDescription("GenericThrowable with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		log("GenericThrowable exception from a simple call", t1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
				.setThrowGenericThrowable(true)
		.setDescription("GenericThrowable with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		log("GenericThrowable exception from an idempotent call", t2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		log("GenericThrowable exception from a repeated idempotent call", t3);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	private void assertEquals(Throwable t1, Throwable t2) {
		Assert.assertNotNull(t1);
		Assert.assertNotNull(t2);
		Assert.assertEquals(t1.getClass(), t2.getClass());
		if (t1 instanceof HystrixRuntimeException) {
			return;
		}
		Assert.assertEquals(t1.getMessage(), t2.getMessage());
	}

	private void log(String message, Throwable t) {
		/*
		LOGGER.info(SEPARATOR);
		LOGGER.info(message, t);
		LOGGER.info(SEPARATOR);
		 */
	}

	private Throwable getExeptionFromSimpleCall(Operation operation) {
		try {
			resource.processSimpleOperation(operation);
			return null;
		}
		catch (final Throwable e) {
			return e;
		}
	}

	private Throwable getExeptionFromIdempotentCall(Long id, Operation operation) {
		try {
			resource.processIdempotentOperation(id, operation);
			return null;
		}
		catch (final Throwable e) {
			return e;
		}
	}
}
