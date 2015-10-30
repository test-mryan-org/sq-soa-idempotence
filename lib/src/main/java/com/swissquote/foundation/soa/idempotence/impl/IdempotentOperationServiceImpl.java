package com.swissquote.foundation.soa.idempotence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swissquote.foundation.soa.idempotence.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotence.Result;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ServiceException;

/**
 * Implementation that fulfills the contract defined by IdempotentOperationService. It generates an identifier that is stored on the server and
 * ensures that when an operation is called with a known unused identifier it is processed once and only once.
 */
public class IdempotentOperationServiceImpl implements IdempotentOperationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentOperationServiceImpl.class);

	private final IdempotentOperationManager operationManager;

	public IdempotentOperationServiceImpl(final IdempotentOperationManager operationManager) {
		this.operationManager = operationManager;
	}

	/**
	 * Method that generates an identifier on the server side and returns it to the client
	 * @return
	 */
	@Override
	public Long generateRequestId() {
		return operationManager.createNewOperation();
	}

	@Override
	public Long generateRequestId(String externalRequestId) {

		return operationManager.createNewOperation(externalRequestId);
	}

	private <T> void processException(final IdempotentOperation<T> operation, Exception exception) {
		LOGGER.debug("Unable to finish correctly due to an exception. Saving the exception ...");

		Result result = operationManager.markAsFailed(operation, exception);

		if (result.failed()) {
			LOGGER.error("Unable to save the exception ...", exception);
		}
	}

	private <T> T processSuccess(final IdempotentOperation<T> operation, T processingResponse) {
		LOGGER.debug("Job processing finished correctly. Saving the response  ...");

		Result result = operationManager.markAsFinished(operation, processingResponse);

		if (result.failed()) {
			LOGGER.debug("Unable to save the result ...");

			return handleUnableToFinish(operation, result);
		}

		LOGGER.debug("Returning the response ...");
		return processingResponse;
	}

	/**
	 * Method that processes in an idempotent manner the operation that is encapsulated in the parameter. It returns either the predefined
	 * response for an "in progress operation" or the result of the encapsulated operation.
	 * @param operation
	 * @return
	 */
	@Override
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public <T> T processIdempotentOperation(final IdempotentOperation<T> operation) throws BusinessCheckedException {

		LOGGER.debug("Marking the operation as 'in progress' ... ");

		Result result = operationManager.markAsInProgress(operation);
		if (result.failed()) {
			LOGGER.debug("Unable to mark the operation as 'in progress'!");

			return handleUnableToStart(operation, result);
		}

		LOGGER.debug("Operation marked as 'in progress'. Starting job processing  ...");

		T processingResponse;
		try {
			processingResponse = operation.process();
		}
		//Get BusinessCheckedException and throw it as it is
		catch (BusinessCheckedException exception) {
			processException(operation, exception);
			LOGGER.warn("Got a BusinessCheckedException", exception);
			throw exception;
		}
		//Get RuntimeException that is not BusinessCheckedException, so throw it as ServiceException
		catch (RuntimeException exception) {
			processException(operation, exception);
			LOGGER.warn("Got a RuntimeException", exception);
			throw new ServiceException(exception);
		}
		//Get a technical exception, throw a ServiceException
		catch (Exception exception) {
			processException(operation, exception);
			LOGGER.warn("Got an Exception", exception);
			throw new ServiceException(exception);
		}
		return processSuccess(operation, processingResponse);
	}

	/**
	 * @param operation
	 * @param result
	 * @return
	 */
	private <T> T handleUnableToStart(final IdempotentOperation<T> operation, final Result result)
			throws BusinessCheckedException {
		switch (result.getReason()) {
			case NO_OPERATION_FOUND:
				return handleNoOperationFound(operation);
			case IN_PROGRESS:
				return handleInProgress(operation);
			case ALREADY_FINISHED_WITH_EXCEPTION:
				throw handleFinishedWithException(result);
			case ALREADY_FINISHED:
				return (T) handleAlreadyFinished(result);
			case UNKNOWN:
			default:
				throw new IllegalArgumentException("Unknown reason!");
		}
	}

	/**
	 * @param operation
	 * @return
	 */
	private <T> T handleNoOperationFound(final IdempotentOperation<T> operation) throws BusinessCheckedException {
		String message = "No operation found for the provided id :" + operation.getRequestId();
		LOGGER.warn(message);
		throw new BusinessCheckedException(message);
	}

	/**
	 * @param operation
	 * @param result
	 * @return
	 */
	private <T> T handleUnableToFinish(@SuppressWarnings("unused") final IdempotentOperation<T> operation,
			@SuppressWarnings("unused") final Result result) {
		String errorMessage = "Failed to update request data to FINISHED status. Concurrent update possible.";
		LOGGER.error(errorMessage);
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * @param operation
	 * @return
	 */
	protected <T> T handleInProgress(final IdempotentOperation<T> operation) {
		LOGGER.debug("The operation is in progress. Returning the predefined response ...");

		return operation.getInProgressResponse();
	}

	/**
	 * @param result
	 * @return
	 */
	protected RuntimeException handleFinishedWithException(final Result result) {
		LOGGER.debug("The operation has finished already. An exception was thrown during its execution. Returning saved exception ...");

		return result.getException();
	}

	/**
	 * @param result
	 * @return
	 */
	protected Object handleAlreadyFinished(final Result result) {
		LOGGER.debug("The operation has finished already. Returning saved response ...");

		return result.getResult();
	}
}
