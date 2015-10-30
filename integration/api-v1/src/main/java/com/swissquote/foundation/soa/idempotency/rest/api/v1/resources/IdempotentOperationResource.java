package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

public interface IdempotentOperationResource {

	Long prepareOperation(OperationSetupRequest request);

}
