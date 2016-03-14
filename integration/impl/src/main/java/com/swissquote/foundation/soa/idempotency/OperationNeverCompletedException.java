package com.swissquote.foundation.soa.idempotency;

public class OperationNeverCompletedException extends RuntimeException {
	public OperationNeverCompletedException(String message) {
		super(message);
	}
}
