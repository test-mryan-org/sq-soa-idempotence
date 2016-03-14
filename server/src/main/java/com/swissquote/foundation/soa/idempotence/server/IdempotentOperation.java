package com.swissquote.foundation.soa.idempotence.server;

public interface IdempotentOperation<T> {

	Class<T> getResponseClass();

	Long getId();

	Object getRequestPayload();

	T getInProgressResponse();

	T process() throws Exception; //NOPMD
}