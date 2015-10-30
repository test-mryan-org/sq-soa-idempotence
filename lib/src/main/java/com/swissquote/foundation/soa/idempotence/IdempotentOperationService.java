package com.swissquote.foundation.soa.idempotence;


import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

/**
 * Interface that defines the contract for handling the calls that need to be idempotent.
 */
public interface IdempotentOperationService {

	/**
	 * Method that generates an identifier on the server side and returns it to the client
	 *
	 * @return
	 */
	Long generateRequestId();

	Long generateRequestId(String externalRequestId);

	/**
	 * Method that processes in an idempotent manner the operation that is encapsulated in the parameter.
	 *
	 * @param operation
	 * @return
	 */
	<T> T processIdempotentOperation(IdempotentOperation<T> operation) throws BusinessCheckedException;
}
