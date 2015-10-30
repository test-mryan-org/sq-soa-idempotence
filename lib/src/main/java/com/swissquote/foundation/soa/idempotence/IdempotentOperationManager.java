package com.swissquote.foundation.soa.idempotence;

import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

public interface IdempotentOperationManager {

	Long createNewOperation();

	<T> Result markAsInProgress(IdempotentOperation<T> operation);

	<T> Result markAsFinished(IdempotentOperation<T> operation, T result);

	<T> Result markAsFailed(IdempotentOperation<T> operation, Throwable t);

	<T> T getResult(IdempotentOperation<T> operation);

	<T> BusinessCheckedException getException(IdempotentOperation<T> operation);
}
