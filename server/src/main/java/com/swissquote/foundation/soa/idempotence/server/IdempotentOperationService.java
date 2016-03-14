package com.swissquote.foundation.soa.idempotence.server;

/**
 * Interface that defines the contract for handling the calls that need to be idempotent.
 */
public interface IdempotentOperationService {

	Long createNewOperation();

	/**
	 * Method that processes in an idempotent manner the operation that is encapsulated in the parameter.
	 * NOTE: this method must be transactional
	 */
	<T> T process(final IdempotentOperation<T> operation);
}
