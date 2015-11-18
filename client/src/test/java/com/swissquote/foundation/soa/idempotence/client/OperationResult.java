package com.swissquote.foundation.soa.idempotence.client;

final class OperationResult {
	private boolean complete;

	public boolean isComplete() {
		return complete;
	}

	public OperationResult setComplete(boolean complete) {
		this.complete = complete;
		return this;
	}
}
