package com.swissquote.foundation.soa.idempotence.server;

public interface IdempotentOperationManager {
	Long createNewOperation();

	<T> Result markAsInProgress(IdempotentOperation<T> operation, String requestAsJson);

	<T> Result markAsFinished(IdempotentOperation<T> operation, String resultAsJson);

	<T> Result markAsFailed(IdempotentOperation<T> operation, String exceptionAsJson);

	<T> Result markAsError(IdempotentOperation<T> operation);

	<T> String getJsonContent(IdempotentOperation<T> operation);
}
