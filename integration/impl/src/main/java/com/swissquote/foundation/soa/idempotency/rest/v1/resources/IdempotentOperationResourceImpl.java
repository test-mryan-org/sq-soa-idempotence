package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import com.swissquote.foundation.soa.idempotence.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationRequest;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationSetupRequest;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public class IdempotentOperationResourceImpl implements IdempotentOperationResource {
	private IdempotentOperationService idempotentOperationService;

	public IdempotentOperationResourceImpl() {
		this.idempotentOperationService = null;
	}

	@Override
	public Long prepareOperation(final OperationSetupRequest request) {
		return Long.valueOf(1);
	}

	public OperationResponse process(final OperationRequest data) throws BusinessCheckedException {

		IdempotentOperation<OperationResponse> operation = new IdempotentOperation<OperationResponse>() {
			@Override
			public Long getRequestId() {
				return data.getRequestId();
			}

			@Override
			public Object getRequestPayload() {
				return data;
			}

			@Override
			public OperationResponse getInProgressResponse() {
				return OperationResponse.builder().inProgress(true).build();
			}

			@Override
			public OperationResponse process() throws BusinessCheckedException {
				return OperationResponse.builder().inProgress(false).build();
			}

			@Override
			public Class<?> getResponseClass() {
				return OperationResponse.class;
			}
		};
		return idempotentOperationService.processIdempotentOperation(operation);
	}

	public IdempotentOperationService getIdempotentOperationService() {
		return idempotentOperationService;
	}

	public void setIdempotentOperationService(IdempotentOperationService idempotentOperationService) {
		this.idempotentOperationService = idempotentOperationService;
	}
}
