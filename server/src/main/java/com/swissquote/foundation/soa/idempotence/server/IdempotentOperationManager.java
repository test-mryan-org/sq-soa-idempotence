package com.swissquote.foundation.soa.idempotence.server;

public interface IdempotentOperationManager {

	Long createNewOperation();

	<T> Result markAsInProgress(IdempotentOperation<T> operation);

	<T> Result markAsFinished(IdempotentOperation<T> operation, T result);

	<T> Result markAsFailed(IdempotentOperation<T> operation, Exception t);

	<T> Result markAsError(IdempotentOperation<T> operation);

	<T> T getResult(IdempotentOperation<T> operation);

	<T> Exception getException(IdempotentOperation<T> operation);
}
