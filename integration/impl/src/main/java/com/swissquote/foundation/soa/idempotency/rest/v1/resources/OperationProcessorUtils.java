package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.WebApplicationException;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

public class OperationProcessorUtils {
	public static final String VARIABLE_NAME = "executionIndex=";

	private static final OperationProcessorUtils INSTANCE = new OperationProcessorUtils();

	private AtomicLong executionsIndex = new AtomicLong(0);

	private OperationProcessorUtils() {
		// private constructor
	}

	public static OperationProcessorUtils getInstance() {
		return INSTANCE;
	}

	public void process(Operation operation) throws BusinessCheckedException {
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
			OperationProcessorUtils.<RuntimeException> throwUnchecked(new Throwable(getExceptionMessage(operation, "Throwable")));
		}

		if (operation.isThrowWebApplicationException()) {
			OperationProcessorUtils.<RuntimeException> throwUnchecked(new WebApplicationException(new RuntimeException(getExceptionMessage(
					operation, "RuntimeException"))));
		}

		if (operation.getSleepMilis() != null) {
			sleep(operation.getSleepMilis());
		}
	}

	private void sleep(Long sleepMilis) {
		try {
			Thread.sleep(sleepMilis);
		}
		catch (InterruptedException e) {
			//
		}
	}

	private String getExceptionMessage(final Operation operation, final String message) {
		return String.format("[%s%d] %s", VARIABLE_NAME, operation.isAddExecutionIndex() ? getExecutionIndex() : 0, message);
	}

	public long getExecutionIndex() {
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
