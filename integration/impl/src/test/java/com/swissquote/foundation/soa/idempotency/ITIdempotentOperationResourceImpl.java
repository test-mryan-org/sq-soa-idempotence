package com.swissquote.foundation.soa.idempotency;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.swissquote.foundation.soa.idempotence.client.IdempotentOperation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.InProgressOperationResponse;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponseWithGsonPolymorphic;
import com.swissquote.foundation.soa.idempotency.rest.v1.resources.OperationProcessorUtils;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-applicationContext.xml"})
public class ITIdempotentOperationResourceImpl {

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
				new Operation().setThrowBusinessCheckedExcetion(true).setAddExecutionIndex(false)
						.setDescription("BusinessCheckedException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
		.setThrowBusinessCheckedExcetion(true)
		.setAddExecutionIndex(false)
		.setDescription("BusinessCheckedException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingABusinessUncheckedException() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
				.setThrowBusinessUncheckedExcetion(true)
				.setAddExecutionIndex(false)
				.setDescription("BusinessUncheckedException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
				.setThrowBusinessUncheckedExcetion(true)
				.setAddExecutionIndex(false)
				.setDescription("BusinessUncheckedException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingAClientException() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
				.setThrowClientException(true)
				.setAddExecutionIndex(false)
				.setDescription("ClientException with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
				.setThrowClientException(true)
				.setAddExecutionIndex(false)
				.setDescription("ClientException with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void throwingGenericThrowable() {
		// a simple operation that throws an exception
		Operation operation1 = new Operation()
				.setThrowGenericThrowable(true)
				.setAddExecutionIndex(false)
				.setDescription("GenericThrowable with a simple call");
		Throwable t1 = getExeptionFromSimpleCall(operation1);
		Assert.assertNotNull(t1);

		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = resource.createNewOperation();
		Operation operation2 = new Operation()
		.setThrowGenericThrowable(true)
		.setAddExecutionIndex(false)
				.setDescription("GenericThrowable with an idempotent call");
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertEquals(t2, t1);

		// Re-executing the same operation a second time
		Throwable t3 = getExeptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t3);
		assertEquals(t3, t1);
	}

	@Test
	public void sameRequestIsExecutedOnlyOnce() throws BusinessCheckedException {
		Long operationId = resource.createNewOperation();
		Operation operation = new Operation().setAddExecutionIndex(true);

		OperationResponse result1 = resource.processIdempotentOperation(operationId, operation);
		OperationResponse result2 = resource.processIdempotentOperation(operationId, operation);

		Assert.assertEquals(result1, result2);
		Assert.assertTrue(result1.getExecutionIndex() > 0);
	}

	@Test
	public void sameRequestIsExecutedTwiceIfTheIdIsNotTheSame() throws BusinessCheckedException {
		Long operationId1 = resource.createNewOperation();
		Operation operation = new Operation().setAddExecutionIndex(true);

		OperationResponse result1 = resource.processIdempotentOperation(operationId1, operation);
		Long operationId2 = resource.createNewOperation();

		OperationResponse result2 = resource.processIdempotentOperation(operationId2, operation);
		Assert.assertNotEquals(result1, result2);
		Assert.assertNotEquals(result1.getExecutionIndex(), result2.getExecutionIndex());
	}

	@Test
	public void onlyOneBusinessCheckedExeptionForTheSameRequestId() {
		Long operationId = resource.createNewOperation();
		Operation operation = new Operation().setAddExecutionIndex(true).setThrowBusinessCheckedExcetion(true);
		Throwable t1 = getExeptionFromIdempotentCall(operationId, operation);
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation);

		assertEquals(t1, t2);
		Assert.assertEquals(BusinessCheckedException.class, t1.getClass());
		assertTheExecutionIndexIsValid(t1);
	}

	@Test
	public void onlyOneBusinessUncheckedExeptionForTheSameRequestId() {
		Long operationId = resource.createNewOperation();
		Operation operation = new Operation().setAddExecutionIndex(true).setThrowBusinessUncheckedExcetion(true);

		Throwable t1 = getExeptionFromIdempotentCall(operationId, operation);
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation);

		assertEquals(t1, t2);
		Assert.assertEquals(BusinessUncheckedException.class, t1.getClass());
		assertTheExecutionIndexIsValid(t1);
	}

	@Test
	public void onlyOneClientExeptionForTheSameRequestId() {
		Long operationId = resource.createNewOperation();
		Operation operation = new Operation().setAddExecutionIndex(true).setThrowClientException(true);

		Throwable t1 = getExeptionFromIdempotentCall(operationId, operation);
		Throwable t2 = getExeptionFromIdempotentCall(operationId, operation);

		assertEquals(t1, t2);
		Assert.assertEquals(ClientException.class, t1.getClass());
		assertTheExecutionIndexIsValid(t1);
	}

	@Test
	public void operationFinishingAfterTheFirstSoaTimeOut() throws BusinessCheckedException {
		/**
		 * The operation is prepared to take 3500ms(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.MyService-V1.properties > read.timeout.millis=3000. This means that we will have the response from the second machine.
		 */
		final Operation operation = new Operation().setAddExecutionIndex(true).setSleepMilis(3500L);
		final AtomicBoolean hadIntermediaryResponse = new AtomicBoolean(false);
		OperationResponse result = new IdempotentOperation<OperationResponse, BusinessCheckedException>(10, 100) {

			@Override
			public Long createNew() {
				return resource.createNewOperation();
			}

			@Override
			public OperationResponse attemptExecution(Long operationId) throws BusinessCheckedException {
				return resource.processIdempotentOperation(operationId, operation);
			}

			@Override
			public boolean isComplete(OperationResponse result) {
				hadIntermediaryResponse.set(true);
				return !result.isInProgress();
			}

			@Override
			public OperationResponse handleNeverCompleted(OperationResponse result) throws BusinessCheckedException {
				throw new BusinessCheckedException("operation did not finish");
			}
		}.execute();

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isInProgress());
		Assert.assertTrue(hadIntermediaryResponse.get());
	}

	@Test(expected = BusinessCheckedException.class)
	public void operationFinishingAfterSoaTimeOut() throws BusinessCheckedException {
		/**
		 * The operation is prepared to take 20s(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.MyService-V1.properties > read.timeout.millis=3000. This means that from the first system we will get a timeout, the
		 * sq-soa layer will switch to the second machine from it it will get a "inProgress" response. It will keep trying (9 more times with a
		 * sleep of 1000ms before retrying) and in the end it will execute the code specified in handleNeverCompleted;
		 */
		final Operation operation = new Operation().setAddExecutionIndex(true).setSleepMilis(20000L);
		OperationResponse result = new IdempotentOperation<OperationResponse, BusinessCheckedException>(10, 1000) {

			@Override
			public Long createNew() {
				return resource.createNewOperation();
			}

			@Override
			public OperationResponse attemptExecution(Long operationId) throws BusinessCheckedException {
				return resource.processIdempotentOperation(operationId, operation);
			}

			@Override
			public boolean isComplete(OperationResponse r) {
				return !r.isInProgress();
			}

			@Override
			public OperationResponse handleNeverCompleted(OperationResponse r) throws BusinessCheckedException {
				throw new BusinessCheckedException("operation did not finish");
			}
		}.execute();
		Assert.assertNotNull(result);
	}

	// @Test
	public void operationFinishingAfterTheFirstSoaTimeOut_GsonPolymorphic() throws BusinessCheckedException {
		/**
		 * The operation is prepared to take 3500ms(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.MyService-V1.properties > read.timeout.millis=3000. This means that we will have the response from the second machine.
		 */
		final Operation operation = new Operation().setAddExecutionIndex(true).setSleepMilis(3500L);
		final AtomicBoolean hadIntermediaryResponse = new AtomicBoolean(false);
		OperationResponseWithGsonPolymorphic result =
				new IdempotentOperation<OperationResponseWithGsonPolymorphic, BusinessCheckedException>(10, 100) {

			@Override
			public Long createNew() {
				return resource.createNewOperation();
			}

			@Override
			public OperationResponseWithGsonPolymorphic attemptExecution(Long operationId) throws BusinessCheckedException {
				return resource.processIdempotentOperation2(operationId, operation);
			}

			@Override
			public boolean isComplete(OperationResponseWithGsonPolymorphic result) {
				hadIntermediaryResponse.set(true);
				return !(result instanceof InProgressOperationResponse);
			}

			@Override
			public OperationResponseWithGsonPolymorphic handleNeverCompleted(OperationResponseWithGsonPolymorphic result)
					throws BusinessCheckedException {
				throw new BusinessCheckedException("operation did not finish");
			}
		}.execute();

		Assert.assertNotNull(result);
		Assert.assertFalse(result instanceof InProgressOperationResponse);
		Assert.assertTrue(hadIntermediaryResponse.get());
	}

	// @Test(expected = BusinessCheckedException.class)
	public void operationFinishingAfterSoaTimeOut_GsonPolymorphic() throws BusinessCheckedException {
		/**
		 * The operation is prepared to take 20s(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.MyService-V1.properties > read.timeout.millis=3000. This means that from the first system we will get a timeout, the
		 * sq-soa layer will switch to the second machine from it it will get a "inProgress" response. It will keep trying (9 more times with a
		 * sleep of 1000ms before retrying) and in the end it will execute the code specified in handleNeverCompleted;
		 */
		final Operation operation = new Operation().setAddExecutionIndex(true).setSleepMilis(20000L);
		OperationResponseWithGsonPolymorphic result =
				new IdempotentOperation<OperationResponseWithGsonPolymorphic, BusinessCheckedException>(10, 1000) {

					@Override
					public Long createNew() {
						return resource.createNewOperation();
					}

					@Override
					public OperationResponseWithGsonPolymorphic attemptExecution(Long operationId) throws BusinessCheckedException {
						return resource.processIdempotentOperation2(operationId, operation);
					}

					@Override
					public boolean isComplete(OperationResponseWithGsonPolymorphic r) {
						return !(r instanceof InProgressOperationResponse);
					}

					@Override
					public OperationResponseWithGsonPolymorphic handleNeverCompleted(OperationResponseWithGsonPolymorphic r)
					throws BusinessCheckedException {
						throw new BusinessCheckedException("operation did not finish");
					}
				}.execute();
		Assert.assertNotNull(result);
	}

	private void assertTheExecutionIndexIsValid(final Throwable t1) {
		String string = t1.getMessage();

		int index = string.indexOf(OperationProcessorUtils.VARIABLE_NAME) + OperationProcessorUtils.VARIABLE_NAME.length();
		string = string.substring(index);
		index = string.indexOf("]");
		string = string.substring(0, index);

		int executionIndex = Integer.parseInt(string);
		Assert.assertTrue(executionIndex > 0);

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
