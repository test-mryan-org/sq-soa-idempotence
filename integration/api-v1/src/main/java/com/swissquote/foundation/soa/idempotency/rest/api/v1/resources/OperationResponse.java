package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("PMD.UnusedPrivateField")
public class OperationResponse {
	private boolean inProgress;
	private long executionIndex;
}
