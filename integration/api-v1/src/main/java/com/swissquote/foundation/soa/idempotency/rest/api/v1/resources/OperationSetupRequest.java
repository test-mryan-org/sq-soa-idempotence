package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@SuppressWarnings("PMD.UnusedPrivateField")
public class OperationSetupRequest {
	private boolean unknown;
	private boolean inProcessing;
	private boolean successful;
	private boolean finishedWithGenericException;
	private boolean finishedWith;

}
