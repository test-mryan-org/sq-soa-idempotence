package com.swissquote.foundation.soa.idempotency;

import com.swissquote.foundation.soa.idempotency.api.v1.entities.Operation;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.OperationResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationProcessorImpl {

	private final OperationProcessorUtils utils;

	public OperationProcessorImpl() {
		utils = OperationProcessorUtils.getInstance();
	}

	public OperationResponse process(final Operation operation) {
		log.info("Processing operation: " + operation.getDescription());
		utils.process(operation);

		return OperationResponse.builder().inProgress(false).executionIndex(utils.getExecutionIndex()).build();
	}

}
