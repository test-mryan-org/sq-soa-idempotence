package com.swissquote.crm.idempotency.api.v1.resources;

public interface IdempotentOperationResource {

	Long prepareOperation(OperationSetupRequest request);

}
