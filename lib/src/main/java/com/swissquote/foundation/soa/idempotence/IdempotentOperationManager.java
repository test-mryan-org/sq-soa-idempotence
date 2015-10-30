package com.swissquote.foundation.soa.idempotence;

public interface IdempotentOperationManager {

	Long createNewOperation();

	Long createNewOperation(String externalRequestId);

	<T> Result markAsInProgress(IdempotentOperation<T> operation);

	<T> Result markAsFinished(IdempotentOperation<T> operation, T result);

	<T> Result markAsFailed(IdempotentOperation<T> operation, Throwable t);
}
