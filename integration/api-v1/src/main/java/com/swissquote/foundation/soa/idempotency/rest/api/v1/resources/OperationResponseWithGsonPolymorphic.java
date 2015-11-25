package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import com.swissquote.foundation.soa.support.api.annotations.GsonPolymorphic;

@GsonPolymorphic
public class OperationResponseWithGsonPolymorphic {
	private long executionIndex;

	public OperationResponseWithGsonPolymorphic() {
		super();
	}

	public long getExecutionIndex() {
		return executionIndex;
	}

	public void setExecutionIndex(long executionIndex) {
		this.executionIndex = executionIndex;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OperationResponseWithGsonPolymorphic [executionIndex=");
		builder.append(executionIndex);
		builder.append("]");
		return builder.toString();
	}
}
