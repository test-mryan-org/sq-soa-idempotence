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
	private final JsonUtils jsonUtils;

	public IdempotentOperationServiceImpl(final IdempotentOperationManager operationManager) {
		this(operationManager, new JsonUtils());
	}

	public IdempotentOperationServiceImpl(final IdempotentOperationManager operationManager, final JsonUtils jsonUtils) {
		this.operationManager = operationManager;
		this.jsonUtils = jsonUtils;
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
	public <T, E extends BusinessCheckedException> T process(final IdempotentOperation<T, E> operation) throws E {

		LOGGER.debug("Marking the operation as 'in progress' ... ");

		Result result = operationManager.markAsInProgress(operation.getId(), jsonUtils.toJson(operation.getRequestPayload()));
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

	private <T, E extends BusinessCheckedException> void processException(final IdempotentOperation<T, E> operation, Exception exception) {
		LOGGER.debug("Unable to finish correctly due to an exception. Saving the exception ...");

		Result result = operationManager.markAsFailed(operation.getId(), jsonUtils.exceptionToJson(exception));

		if (result.failed()) {
			LOGGER.warn("Unable to save the exception ...", exception);
		}
	}

	private <T, E extends BusinessCheckedException> T processSuccess(final IdempotentOperation<T, E> operation, T processingResponse) {
		LOGGER.debug("Job processing finished correctly. Saving the response  ...");

		String jsonResponse = jsonUtils.toJson(processingResponse);

		Result result = operationManager.markAsFinished(operation.getId(), jsonResponse);

		if (result.failed()) {
			LOGGER.debug("Unable to save the result ...");

			return handleUnableToFinish(operation, result);
		}

		LOGGER.debug("Returning the response ...");
		return processingResponse;
	}

	private <T, E extends BusinessCheckedException> void processUnrecoverableException(final IdempotentOperation<T, E> operation) {
		LOGGER.debug("Unable to finish correctly due to an unrecoverable exception. Marking the operation as error for later retry ...");

		Result result = operationManager.markAsError(operation.getId());

		if (result.failed()) {
			LOGGER.warn("Unable to mark the operation with error ...");
		}
	}

	/**
	 * @param operation
	 * @param result
	 * @return
	 */
	private <T, E extends BusinessCheckedException> T handleUnableToStart(final IdempotentOperation<T, E> operation, final Result result)
			throws E {
		switch (result.getReason()) {
			case NO_OPERATION_FOUND:
				return handleNoOperationFound(operation);
			case IN_PROGRESS:
				return handleInProgress(operation);
			case ALREADY_FINISHED_WITH_EXCEPTION:
				handleFinishedWithException(operation, result);
				break;
			case ALREADY_FINISHED:
				return handleAlreadyFinished(operation, result);
			case UNKNOWN:
			default:
				throw new IllegalArgumentException("Unknown reason!");
		}
		// well, this is a side effect of the dirty hack from handleFinishedWithException
		return null;
	}

	/**
	 * @param operation
	 * @return
	 */
	private <T, E extends BusinessCheckedException> T handleNoOperationFound(final IdempotentOperation<T, E> operation) {
		String message = "No operation found for the provided id :" + operation.getId();
		LOGGER.warn(message);
		throw new IllegalStateException(message);
	}

	/**
	 * @param operation
	 * @param result
	 * @return
	 */
	private <T, E extends BusinessCheckedException> T handleUnableToFinish(
			@SuppressWarnings("unused") final IdempotentOperation<T, E> operation,
			@SuppressWarnings("unused") final Result result) {
		String errorMessage = "Failed to update request data to FINISHED status. Concurrent update possible.";
		LOGGER.warn(errorMessage);
		throw new IllegalStateException(errorMessage);
	}

	/**
	 * @param operation
	 * @return
	 */
	protected <T, E extends BusinessCheckedException> T handleInProgress(final IdempotentOperation<T, E> operation) {
		LOGGER.debug("The operation is in progress. Returning the predefined response ...");
		return operation.getInProgressResponse();
	}

	/**
	 * @param result
	 * @return
	 */
	protected <T, E extends BusinessCheckedException> void handleFinishedWithException(final IdempotentOperation<T, E> operation,
			final Result result) {
		LOGGER.debug("The operation has finished already. An exception was thrown during its execution. Returning saved exception ...");

		String json = operationManager.getJsonContent(operation.getId());

		IdempotentOperationServiceImpl.<RuntimeException> throwUnchecked(jsonUtils.exceptionFromJson(json));
	}

	/**
	 * @param result
	 * @return
	 */
	protected <T, E extends BusinessCheckedException> T handleAlreadyFinished(final IdempotentOperation<T, E> operation, final Result result) {
		LOGGER.debug("The operation has finished already. Returning saved response ...");

		String json = operationManager.getJsonContent(operation.getId());

		return (T) jsonUtils.fromJson(json, operation.getResponseClass());
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
