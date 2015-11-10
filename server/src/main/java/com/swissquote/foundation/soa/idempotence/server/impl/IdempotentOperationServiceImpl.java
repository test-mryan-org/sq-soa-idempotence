package com.swissquote.foundation.soa.idempotence.server.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotence.server.Result;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessUncheckedException;
import com.swissquote.foundation.soa.support.api.exceptions.ClientException;

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
	public Long createNewOperation() {
		return operationManager.createNewOperation();
	}

	/**
	 * Method that processes in an idempotent manner the operation that is encapsulated in the parameter. It returns either the predefined
	 * response for an "in progress operation" or the result of the encapsulated operation.
	 * @param operation
	 * @return
	 * @throws BusinessCheckedException
	 */
	@Override
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public <T> T process(final IdempotentOperation<T> operation) throws BusinessCheckedException {

		LOGGER.debug("Marking the operation as 'in progress' ... ");

		Result result = operationManager.markAsInProgress(operation);
		if (result.failed()) {
			LOGGER.debug("Unable to mark the operation as 'in progress'!");

			return handleUnableToStart(operation, result);
		}

		LOGGER.debug("Operation marked as 'in progress'. Starting job processing  ...");

		T processingResponse = null;
		try {
			processingResponse = operation.process();
		}
		catch (BusinessCheckedException exception) {
			LOGGER.warn("Got a BusinessCheckedException", exception);
			processException(operation, exception);
			throw exception;
		}
		catch (BusinessUncheckedException exception) {
			LOGGER.warn("Got a BusinessUncheckedException", exception);
			processException(operation, exception);
			throw exception;
		}
		catch (ClientException exception) {
			LOGGER.warn("Got a ClientException", exception);
			processException(operation, exception);
			throw exception;
		}
		catch (Throwable t) {
			processUnrecoverableException(operation);
			throw t;
		}
		return processSuccess(operation, processingResponse);
	}

	private <T> void processException(final IdempotentOperation<T> operation, Exception exception) {
		LOGGER.debug("Unable to finish correctly due to an exception. Saving the exception ...");

		Result result = operationManager.markAsFailed(operation, exception);

		if (result.failed()) {
			LOGGER.warn("Unable to save the exception ...", exception);
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

	private <T> void processUnrecoverableException(final IdempotentOperation<T> operation) {
		LOGGER.debug("Unable to finish correctly due to an unrecoverable exception. Marking the operation as error for later retry ...");

		Result result = operationManager.markAsError(operation);

		if (result.failed()) {
			LOGGER.warn("Unable to mark the operation with error ...");
		}
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
				handleFinishedWithException(operation, result);
			case ALREADY_FINISHED:
				return handleAlreadyFinished(operation, result);
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
		LOGGER.warn(errorMessage);
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
	protected <T> void handleFinishedWithException(final IdempotentOperation<T> operation, final Result result) {
		LOGGER.debug("The operation has finished already. An exception was thrown during its execution. Returning saved exception ...");
		IdempotentOperationServiceImpl.<RuntimeException> throwUnchecked(operationManager.getException(operation));
	}

	/**
	 * @param result
	 * @return
	 */
	protected <T> T handleAlreadyFinished(final IdempotentOperation<T> operation, final Result result) {
		LOGGER.debug("The operation has finished already. Returning saved response ...");
		return operationManager.getResult(operation);
	}

	/*
	 * A dirty hack to allow us to throw exceptions of any type without bringing down the unsafe
	 * thunder.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Exception> void throwUnchecked(Throwable e) throws T {
		throw (T) e;
	}
}
