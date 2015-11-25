package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public class OperationProcessorImpl implements OperationProcessor<OperationResponse> {
	private final OperationProcessorUtils utils;

	public OperationProcessorImpl() {
		utils = OperationProcessorUtils.getInstance();
	}

	@Override
	public OperationResponse process(final Operation operation) throws BusinessCheckedException {
		utils.process(operation);

		return OperationResponse.builder().inProgress(false).executionIndex(utils.getExecutionIndex()).build();
	}

}
