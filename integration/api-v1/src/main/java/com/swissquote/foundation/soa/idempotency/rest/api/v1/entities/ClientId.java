package com.swissquote.foundation.soa.idempotency.rest.api.v1.entities;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ClientId {

	@NotNull
	@Size(max = 6)
	private String client;

	// used to serialize ClientID when used as parameters in URLs for proxy invocations
	@Override
	public String toString() {
		return client;
	}

	// For deserialization of parameters in URLs
	public static ClientId valueOf(String client) {
		return new ClientId(client);
	}

}