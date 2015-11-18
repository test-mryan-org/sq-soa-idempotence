package com.swissquote.foundation.soa.idempotence.client;

public class Factories {
	public static OperationResultFactory createCompletingIn(final int retries) {
		if (retries < 0) {
			throw new IllegalArgumentException("the value cannot be negative");
		}

		return new OperationResultFactory() {
			private int counter = retries;

			@Override
			public OperationResult createOperationResult() {
				if (counter == 1) {
					return new OperationResult().setComplete(true);
				}
				counter--;
				return new OperationResult().setComplete(false);
			}
		};

	}
}
