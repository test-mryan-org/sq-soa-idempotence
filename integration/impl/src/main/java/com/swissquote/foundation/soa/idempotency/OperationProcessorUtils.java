package com.swissquote.foundation.soa.idempotency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.swissquote.foundation.soa.idempotency.api.v1.entities.Operation;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;
import com.swissquote.foundation.soa.support.api.exceptions.DetailedWebApplicationException;

@SuppressWarnings("PMD")
public final class OperationProcessorUtils {
	public static final String VARIABLE_NAME = "executionIndex";

	private static final OperationProcessorUtils INSTANCE = new OperationProcessorUtils();

	private final AtomicLong executionsIndex = new AtomicLong(0);

	private OperationProcessorUtils() {
		// private constructor
	}

	public static OperationProcessorUtils getInstance() {
		return INSTANCE;
	}

	public void process(Operation operation) {
		if (operation.isThrowBusinessUncheckedException()) {
			throw new BusinessUncheckedException(getExceptionMessage(operation, "BusinessUncheckedException"));
		}

		if (operation.isThrowClientException()) {
			throw new ClientException(getExceptionMessage(operation, "ClientException"));
		}

		if (operation.isThrowRuntimeException()) {
			throw new RuntimeException(getExceptionMessage(operation, "Throwable"));
		}

		if (operation.isThrowWebApplicationException()) {
			throw new WebApplicationException(new RuntimeException(getExceptionMessage(operation, "RuntimeException")));
		}

		if (operation.isThrowDetailedWebAppException()) {

			Map<String, String> details = new HashMap<>();
			details.put(VARIABLE_NAME, getExecutionIndex(operation).toString());
			throw new DetailedWebApplicationException("DetailedWebAppException", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
					"ERROR_CODE", details);
		}

		if (operation.getSleepMillis() != null) {
			sleep(operation.getSleepMillis());
		}
	}

	private void sleep(Long sleepMillis) {
		try {
			Thread.sleep(sleepMillis);
		}
		catch (InterruptedException e) {
			//
		}
	}

	private String getExceptionMessage(final Operation operation, final String message) {
		return String.format("[%s=%d] %s", VARIABLE_NAME, getExecutionIndex(operation), message);
	}

	private Long getExecutionIndex(Operation operation) {
		return operation.isAddExecutionIndex() ? getExecutionIndex() : 0;
	}

	public long getExecutionIndex() {
		return executionsIndex.incrementAndGet();
	}

}
