package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import java.util.concurrent.atomic.AtomicLong;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

public class OperationProcessorImpl implements OperationProcessor {
	public static final String VARIABLE_NAME = "executionIndex=";

	private static final OperationProcessorImpl INSTANCE = new OperationProcessorImpl();

	private AtomicLong executionsIndex = new AtomicLong(0);

	private OperationProcessorImpl() {
		// private matters
	}

	public static OperationProcessorImpl instance() {
		return INSTANCE;
	}

	@Override
	public OperationResponse process(final Operation operation) throws BusinessCheckedException {
		if (operation.isThrowBusinessCheckedExcetion()) {
			throw new BusinessCheckedException(getExceptionMessage(operation, "BusinessCheckedException"));
		}

		if (operation.isThrowBusinessUncheckedExcetion()) {
			throw new BusinessUncheckedException(getExceptionMessage(operation, "BusinessUncheckedException"));
		}

		if (operation.isThrowClientException()) {
			throw new ClientException(getExceptionMessage(operation, "ClientException"));
		}

		if (operation.isThrowGenericThrowable()) {
			OperationProcessorImpl.<RuntimeException> throwUnchecked(new Throwable(getExceptionMessage(operation, "Throwable")));
		}

		return OperationResponse.builder().inProgress(false).executionIndex(getExecutionIndex()).build();
	}

	private String getExceptionMessage(final Operation operation, final String message) {
		return String.format("[%s%d] %s", VARIABLE_NAME, operation.isAddExecutionIndex() ? getExecutionIndex() : 0, message);
	}

	private long getExecutionIndex() {
		return executionsIndex.incrementAndGet();
	}

	/*
	 * A dirty hack to allow us to throw exceptions of any type without bringing down the unsafe
	 * thunder.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Exception> void throwUnchecked(Throwable e) throws T {
		throw (T) e;
	}

}
