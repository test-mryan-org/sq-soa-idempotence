package com.swissquote.foundation.soa.idempotence.server.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.eclipse.jetty.io.RuntimeIOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.server.JsonUtils;
import com.swissquote.foundation.soa.idempotence.server.Result;

@RunWith(MockitoJUnitRunner.class)
public class IdempotentOperationServiceImplTest {

	private static final Long OPERATION_ID = 100L;
	private static final String PAYLOAD = "{}";
	private static final Integer RESPONSE = 1000;
	private static final Integer IN_PROGRESS_RESPONSE = 1000;
	private static final String RESPONSE_JSON = "{{}}";
	@InjectMocks
	private IdempotentOperationServiceImpl idempotentOperationService;

	@Mock
	private JsonUtils jsonUtils;

	@Mock
	private IdempotentOperationManager operationManager;

	@Test
	public void testCreateNewOperation() throws Exception {
		idempotentOperationService.createNewOperation();
		verify(operationManager).createNewOperation();
	}

	@Test
	public void testProcess_newOperation_runsSuccessfully() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();
		when(operation.process()).thenReturn(RESPONSE);

		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);
		when(jsonUtils.toJson(RESPONSE)).thenReturn(RESPONSE_JSON);

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.success());
		when(operationManager.markAsFinished(OPERATION_ID, RESPONSE_JSON)).thenReturn(Result.success());

		Integer processingResult = idempotentOperationService.process(operation);
		verify(operation).process();
		assertEquals(RESPONSE, processingResult);
	}

	@Test(expected = WebApplicationException.class)
	public void testProcess_newOperation_throwsException() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();
		RuntimeIOException exception = new RuntimeIOException();
		WebApplicationException mappedException = new WebApplicationException();
		when(operation.process()).thenThrow(exception);

		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);
		when(jsonUtils.mapException(exception)).thenReturn(mappedException);
		when(jsonUtils.mappedExceptionToJson(mappedException)).thenReturn(RESPONSE_JSON);

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.success());
		when(operationManager.markAsFailed(OPERATION_ID, RESPONSE_JSON)).thenReturn(Result.success());

		idempotentOperationService.process(operation);
		verify(operation).process();
	}

	@Test
	public void testProcess_alreadyInProgress() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();

		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.fail(Result.Reason.IN_PROGRESS));

		Integer processingResult = idempotentOperationService.process(operation);
		verify(operation, never()).process();
		assertEquals(IN_PROGRESS_RESPONSE, processingResult);
	}

	@Test
	public void testProcess_alreadyFinished() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();
		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);
		when(jsonUtils.fromJson(RESPONSE_JSON, Integer.class)).thenReturn(RESPONSE);

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.fail(Result.Reason.ALREADY_FINISHED));
		when(operationManager.getJsonContent(OPERATION_ID)).thenReturn(RESPONSE_JSON);

		Integer processingResult = idempotentOperationService.process(operation);
		verify(operation, never()).process();
		assertEquals(RESPONSE, processingResult);
	}

	@Test(expected = WebApplicationException.class)
	public void testProcess_alreadyFinishedWithException() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();
		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);
		when(jsonUtils.exceptionFromJson(RESPONSE_JSON)).thenReturn(new WebApplicationException());

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.fail(Result.Reason.ALREADY_FINISHED_WITH_EXCEPTION));
		when(operationManager.getJsonContent(OPERATION_ID)).thenReturn(RESPONSE_JSON);

		idempotentOperationService.process(operation);
		verify(operation, never()).process();
	}

	@Test(expected = WebApplicationException.class)
	public void testProcess_operationNotFound() throws Exception {
		IdempotentOperation<Integer> operation = mockIntegerIdempotentOperation();
		when(jsonUtils.toJson(any())).thenReturn(PAYLOAD);

		when(operationManager.markAsInProgress(OPERATION_ID, PAYLOAD)).thenReturn(Result.fail(Result.Reason.NO_OPERATION_FOUND));

		idempotentOperationService.process(operation);
		verify(operation, never()).process();
	}

	@SuppressWarnings("unchecked")
	private IdempotentOperation<Integer> mockIntegerIdempotentOperation() {
		IdempotentOperation<Integer> operation = mock(IdempotentOperation.class);
		when(operation.getId()).thenReturn(OPERATION_ID);
		when(operation.getResponseClass()).thenReturn(Integer.class);
		when(operation.getInProgressResponse()).thenReturn(IN_PROGRESS_RESPONSE);
		return operation;
	}
}