package com.swissquote.foundation.soa.idempotence.client;

public abstract class IdempotentOperation<T, E extends Throwable> {
	public T execute() throws E {
		Long operationId = createNew();

		T result = executeWrappedOperation(operationId);
		while (!isComplete(result) && stillRetry()) {
			sleep();
			result = executeWrappedOperation(operationId);
			retried();
		}

		if (isComplete(result)) {
			return result;
		}
		return handleNotCompletedResult();
	}

	public abstract Long createNew();

	public abstract T executeWrappedOperation(Long operationId) throws E;

	public abstract boolean isComplete(T result);

	public abstract boolean stillRetry();

	public abstract void sleep();

	public abstract T handleNotCompletedResult();

	public abstract void retried();
}
