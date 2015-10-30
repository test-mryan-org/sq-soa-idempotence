package com.swissquote.crm.idempotency.api.v1.resources;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("PMD.UnusedPrivateField")
public class OperationRequest {
	private Long requestId;
}
