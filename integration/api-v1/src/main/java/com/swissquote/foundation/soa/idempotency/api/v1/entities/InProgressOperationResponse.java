package com.swissquote.foundation.soa.idempotency.api.v1.entities;

public class InProgressOperationResponse extends OperationResponse {

	public InProgressOperationResponse() {
		super(true, -1L);
	}

	@Override
	public String toString() {
		return "InProgressOperationResponse []";
	}
}
