package com.swissquote.foundation.soa.idempotence.server;

import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

/**
 * Interface that defines the contract for handling the calls that need to be idempotent.
 */
public interface IdempotentOperationService {

	/**
	 * Method that generates an identifier on the server side and returns it to the client
	 * @return
	 */
	Long createNewOperation();

	/**
	 * Method that sets externalId as identifier on the server side and returns it to the client
	 * @return
	 */
	Long createNewOperationWithExternalId(String externalId);

	/**
	 * Method that processes in an idempotent manner the operation that is encapsulated in the parameter.
	 * @param operation
	 * @return
	 */
	<T, E extends BusinessCheckedException> T process(IdempotentOperation<T, E> operation) throws E;
}
