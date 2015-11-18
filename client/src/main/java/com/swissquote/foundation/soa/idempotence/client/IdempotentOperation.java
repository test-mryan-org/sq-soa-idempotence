package com.swissquote.foundation.soa.idempotence.client;

/**
 * Use this wrapper on the client side if you are implementing an idempotent operation and you have to protect your code from responses that
 * could be incomplete (the client receives an answer from the server, but that is not the business one it is expecting but one sent by the
 * server side layer that deals with operations that have already been started but have not yet finished ).
 * @author Andrei Niculescu (andrei.niculescu@swissquote.ch)
 * @param <T> The type of your response
 * @param <E> The exception that could be thrown by the soa code (usually a BusinessCheckedException)
 */
public abstract class IdempotentOperation<T, E extends Throwable> {
	private static final int NUBER_OF_RETRIES = 9;
	private static final int SLEEP_MILIS_BEFORE_RETRYING = 4000;
	private int retries;
	private int sleepMilis;

	public IdempotentOperation() {
		this(NUBER_OF_RETRIES, SLEEP_MILIS_BEFORE_RETRYING);
	}

	public IdempotentOperation(final int retries, final int sleepMilis) {
		if (retries < 0) {
			throw new IllegalArgumentException("'retries' cannot be negative");
		}
		this.retries = retries;

		if (sleepMilis < 0) {
			throw new IllegalArgumentException("'sleepMilis' cannot be negative");
		}
		this.sleepMilis = sleepMilis;
	}

	public synchronized T execute() throws E {
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
		return handleNeverCompleted(result);
	}

	public abstract Long createNew();

	public abstract T attemptExecution(final Long operationId) throws E;

	public abstract boolean isComplete(final T result);

	public synchronized boolean canRetry() {
		return retries > 0;
	}

	public abstract T handleNeverCompleted(T result) throws E;

	public void sleep() {
		try {
			Thread.sleep(sleepMilis);
		}
		catch (InterruptedException e) {
			// do nothing if interrupted
		}
	}

	public synchronized void retried() {
		retries--;
	}

	public int getRetries() {
		return retries;
	}

	public int getSleepMilis() {
		return sleepMilis;
	}

}
