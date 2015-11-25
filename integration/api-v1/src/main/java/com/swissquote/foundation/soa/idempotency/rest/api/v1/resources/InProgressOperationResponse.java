package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import com.swissquote.foundation.soa.support.api.annotations.GsonPolymorphic;

@GsonPolymorphic
public class InProgressOperationResponse extends OperationResponseWithGsonPolymorphic {

	public InProgressOperationResponse() {
		super();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InProgressOperationResponse []");
		return builder.toString();
	}
}
