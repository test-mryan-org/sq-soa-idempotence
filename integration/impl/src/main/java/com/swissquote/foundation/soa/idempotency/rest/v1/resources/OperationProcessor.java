package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public interface OperationProcessor {
	OperationResponse process(Operation operation) throws BusinessCheckedException;
}
