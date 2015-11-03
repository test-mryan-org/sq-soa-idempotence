package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

public class OperationProcessorImpl implements OperationProcessor {
	private static final OperationProcessorImpl INSTANCE = new OperationProcessorImpl();

	private OperationProcessorImpl() {
		// private matters
	}

	public static OperationProcessorImpl instance() {
		return INSTANCE;
	}

	@Override
	public OperationResponse process(final Operation operation) throws BusinessCheckedException {
		if (operation.isThrowBusinessCheckedExcetion()) {
			throw new BusinessCheckedException("This is a BusinessCheckedException exception...");
		}

		if (operation.isThrowBusinessUncheckedExcetion()) {
			throw new BusinessUncheckedException("This is a BusinessUncheckedException...");
		}

		if (operation.isThrowClientException()) {
			throw new ClientException("This is a ClientException...");
		}

		if (operation.isThrowGenericThrowable()) {
			OperationProcessorImpl.<RuntimeException> throwUnchecked(new Throwable("Throable exception"));
		}

		return null;
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
