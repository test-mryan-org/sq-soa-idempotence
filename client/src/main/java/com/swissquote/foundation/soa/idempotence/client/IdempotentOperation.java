package com.swissquote.foundation.soa.idempotence.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this wrapper on the client side if you are implementing an idempotent operation and you have to protect your code from responses that
 * could be incomplete (the client receives an answer from the server, but that is not the business one it is expecting but one sent by the
 * server side layer that deals with operations that have already been started but have not yet finished ).
 * @param <T> The type of your response
 * @param <E> The exception that could be thrown by the soa code (usually a BusinessCheckedException)
 * @author Andrei Niculescu (andrei.niculescu@swissquote.ch)
 */
public abstract class IdempotentOperation<T, E extends Throwable> {
	private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentOperation.class);

	private static final int NUBER_OF_CALLS = 20;
	private static final int SLEEP_MILIS_BEFORE_RETRYING = 4000;
	private int noOfCalls;
	private int sleepMillis;

	public IdempotentOperation() {
		this(NUBER_OF_CALLS, SLEEP_MILIS_BEFORE_RETRYING);
	}

	public IdempotentOperation(final int noOfCalls, final int sleepMillis) {
		if (noOfCalls <= 0) {
			throw new IllegalArgumentException("'noOfCalls' has to be a positive and greater than 0");
		}
		this.noOfCalls = noOfCalls;

		if (sleepMillis <= 0) {
			throw new IllegalArgumentException("'sleepMillis' has to be a positive and greater than 0");
		}
		this.sleepMillis = sleepMillis;
	}

	public T execute() throws E {
		Long operationId = createNew();

		LOGGER.debug("Using operationId = {}", operationId);

		T result = attemptExecution(operationId);
		callPerformed();
		while (!isComplete(result) && canRetry()) {
			sleep();
			LOGGER.info("Received 'inProgress' response for operationId = {}. Retrying ...", operationId);
			result = attemptExecution(operationId);
			callPerformed();
		}

		if (isComplete(result)) {
			return result;
		}

		LOGGER.warn("Operation [{}] did not complete ... ", operationId);
		return handleNeverCompleted(result);
	}

	public abstract Long createNew();

	public abstract T attemptExecution(final Long operationId) throws E;

	public abstract boolean isComplete(final T result);

	public boolean canRetry() {
		return noOfCalls > 0;
	}

	public abstract T handleNeverCompleted(T result) throws E;

	public void sleep() {
		try {
			Thread.sleep(sleepMillis);
		}
		catch (InterruptedException e) {
			// do nothing if interrupted
			LOGGER.warn("Interruption occurred", e);
		}
	}

	public void callPerformed() {
		noOfCalls--;
	}

	public int getNoOfCalls() {
		return noOfCalls;
	}

	public int getSleepMilis() {
		return sleepMillis;
	}

}
