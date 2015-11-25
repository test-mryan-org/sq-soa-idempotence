package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponseWithGsonPolymorphic;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public class OperationProcessor2Impl implements OperationProcessor<OperationResponseWithGsonPolymorphic> {
	private final OperationProcessorUtils utils;

	public OperationProcessor2Impl() {
		utils = OperationProcessorUtils.getInstance();
	}

	@Override
	public OperationResponseWithGsonPolymorphic process(final Operation operation) throws BusinessCheckedException {
		utils.process(operation);

		OperationResponseWithGsonPolymorphic result = new OperationResponseWithGsonPolymorphic();
		result.setExecutionIndex(utils.getExecutionIndex());
		return result;
	}
}
