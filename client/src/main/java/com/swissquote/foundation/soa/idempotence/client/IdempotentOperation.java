package com.swissquote.foundation.soa.idempotence.client;

public abstract class IdempotentOperation<T, E extends Throwable> {
	private int retries;
	private int sleepMilis;

	public IdempotentOperation() {
		this(5, 2000);
	}

	public IdempotentOperation(final int retries, final int sleeepMilis) {
		if (retries > 0) {
			this.retries = retries;
		}
		if (sleepMilis > 0) {
			this.sleepMilis = sleeepMilis;
		}
	}

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

	public abstract T attemptExecution(final Long operationId) throws E;

	public abstract boolean isComplete(final T result);

	public boolean canRetry() {
		return retries > 0;
	}

	public abstract T handleNotCompletedResult(T result);

	public void sleep() {
		try {
			Thread.sleep(sleepMilis);
		}
		catch (InterruptedException e) {
			// do nothing if interrupted
		}
	}

	public void retried() {
		retries--;
	}
}
