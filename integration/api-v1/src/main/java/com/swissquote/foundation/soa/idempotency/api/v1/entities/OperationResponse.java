package com.swissquote.foundation.soa.idempotency.api.v1.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("PMD.UnusedPrivateField")
public class OperationResponse {
	private boolean inProgress;
	private long executionIndex;
}
