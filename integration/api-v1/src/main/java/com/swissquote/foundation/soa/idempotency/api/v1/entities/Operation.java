package com.swissquote.foundation.soa.idempotency.api.v1.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Operation {
	private String description;
	private boolean throwDetailedWebAppException;
	private boolean throwBusinessUncheckedException;
	private boolean throwClientException;
	private boolean throwWebApplicationException;
	private boolean throwRuntimeException;
	private boolean addExecutionIndex;
	private Long sleepMillis;

}
