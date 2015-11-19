package com.swissquote.foundation.soa.idempotence.server;

import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public interface IdempotentOperation<T> {

	Class<?> getResponseClass();

	Long getId();

	Object getRequestPayload();

	T getInProgressResponse();

	T process() throws BusinessCheckedException;
}