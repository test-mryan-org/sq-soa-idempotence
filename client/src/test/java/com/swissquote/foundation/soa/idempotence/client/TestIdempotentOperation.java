package com.swissquote.foundation.soa.idempotence.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

public class TestIdempotentOperation {
	private static final int SLEEP_MILIS = 1;
	private final AtomicLong operationIdGenerator = new AtomicLong(0);

	@Test(expected = IllegalArgumentException.class)
	public void negativeRetriesNo() {
		createIdempotentOperation(-1, 100);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeSleepTime() {
		createIdempotentOperation(10, -1);
	}

	@Test
	public void objectCorrectlyBuilt() {
		ClientIdempotentOperation<OperationResult> opertion = createIdempotentOperation(10, 1000);
		Assert.assertEquals(10, opertion.getNoOfCalls());
		Assert.assertEquals(1000, opertion.getSleepMillis());
	}

	private ClientIdempotentOperation<OperationResult> createIdempotentOperation(final int noOfCalls, final int sleepMilis) {
		return createIdempotentOperation(noOfCalls, sleepMilis, 10);
	}

	private ClientIdempotentOperation<OperationResult> createIdempotentOperation(final int noOfCalls, final int sleepMilis,
			final int completeAfter) {
		final OperationResultFactory factory = Factories.createCompletingIn(completeAfter);

		return new ClientIdempotentOperation<OperationResult>(noOfCalls, sleepMilis) {
			@Override
			public Long createNew() {
				return operationIdGenerator.incrementAndGet();
			}

			@Override
			public OperationResult attemptExecution(final Long operationId) {
				return factory.createOperationResult();
			}

			@Override
			public boolean isComplete(final OperationResult result) {
				return result.isComplete();
			}

			@Override
			public OperationResult handleNeverCompleted(final OperationResult result) {
				throw new OperationException();
			}

		};
	}

	@Test
	public void sameOperationIdIsUsedForAllCalls() throws OperationException {
		final Set<Long> operationIds = new HashSet<Long>();
		final OperationResultFactory factory = Factories.createCompletingIn(8);

		new ClientIdempotentOperation<OperationResult>(10, 1) {
			@Override
			public Long createNew() {
				return operationIdGenerator.incrementAndGet();
			}

			@Override
			public OperationResult attemptExecution(final Long operationId) throws OperationException {
				operationIds.add(operationId);
				return factory.createOperationResult();
			}

			@Override
			public boolean isComplete(final OperationResult result) {
				return result.isComplete();
			}

			@Override
			public OperationResult handleNeverCompleted(final OperationResult result) throws OperationException {
				throw new OperationException();
			}
		}.execute();

		Assert.assertEquals(1, operationIds.size());
	}

	@Test(expected = OperationException.class)
	public void operationNeverCompletes() throws OperationException {
		OperationResult result = createIdempotentOperation(10, SLEEP_MILIS, Integer.MAX_VALUE).execute();
	}

	@Test
	public void operationCompetesCorrectly() throws OperationException {
		OperationResult result = createIdempotentOperation(10, SLEEP_MILIS, 10).execute();
		Assert.assertNotNull(result);
		Assert.assertTrue(result.isComplete());
	}
}
