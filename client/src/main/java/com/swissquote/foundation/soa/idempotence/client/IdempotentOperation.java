package com.swissquote.foundation.soa.idempotence.client;

public abstract class IdempotentOperation<T, E extends Throwable> {
	public T execute() throws E {
		Long operationId = createNew();

		T result = attemptExecution(operationId);
		while (!isComplete(result) && canRetry()) {
			sleep();
			result = attemptExecution(operationId);
			retried();
		}

		if (isComplete(result)) {
			return result;
		}
		return handleNotCompletedResult(result);
	}

	public abstract Long createNew();

	public abstract T attemptExecution(Long operationId) throws E;

	public abstract boolean isComplete(T result);

	public abstract boolean canRetry();

	public abstract void sleep();

	public abstract T handleNotCompletedResult(T result);

	public abstract void retried();
}
