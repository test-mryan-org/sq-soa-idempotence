package com.swissquote.foundation.soa.idempotence.server.impl;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotence.server.JsonUtils;
import com.swissquote.foundation.soa.idempotence.server.Result;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation that fulfills the contract defined by IdempotentOperationService. It generates an identifier that is stored on the server and
 * ensures that when an operation is called with a known unused identifier it is processed once and only once.
 */
@Slf4j
@AllArgsConstructor
public class IdempotentOperationServiceImpl implements IdempotentOperationService {
	private final IdempotentOperationManager operationManager;
	private final JsonUtils jsonUtils;

	@Override
	public Long createNewOperation() {
		return operationManager.createNewOperation();
	}

	@Override
	@SuppressWarnings("PMD.AvoidCatchingGenericException")
	public <T> T process(final IdempotentOperation<T> operation) {

		log.debug("Marking the operation as 'in progress' ... ");

		Result result = operationManager.markAsInProgress(operation.getId(), jsonUtils.toJson(operation.getRequestPayload()));
		if (result.failed()) {
			log.debug("Unable to mark the operation as 'in progress'!");

			return handleUnableToStart(operation, result);
		}

		log.debug("Operation marked as 'in progress'. Starting job processing  ...");

		try {
			T processingResponse = operation.process();
			return processSuccess(operation, processingResponse);
		}
		catch (Exception exception) {
			log.warn(String.format("Got a %s", exception.getClass().getSimpleName()), exception);
			throw processException(operation, exception);
		}
	}

	private <T> WebApplicationException processException(final IdempotentOperation<T> operation, Exception exception) {
		log.debug("Unable to finish correctly due to an exception. Saving the exception ...");

		WebApplicationException mappedException = jsonUtils.mapException(exception);
		Result result = operationManager.markAsFailed(operation.getId(), jsonUtils.mappedExceptionToJson(mappedException));

		if (result.failed()) {
			log.warn("Unable to save the exception ...", exception);
		}

		return mappedException;
	}

	private <T> T processSuccess(final IdempotentOperation<T> operation, T processingResponse) {
		log.debug("Job processing finished correctly. Saving the response  ...");

		String jsonResponse = jsonUtils.toJson(processingResponse);

		Result result = operationManager.markAsFinished(operation.getId(), jsonResponse);

		if (result.failed()) {
			log.debug("Unable to save the result ...");

			return handleUnableToFinish();
		}

		log.debug("Returning the response ...");
		return processingResponse;
	}

	private <T> T handleUnableToStart(final IdempotentOperation<T> operation, final Result result) {
		switch (result.getReason()) {
			case NO_OPERATION_FOUND:
				return handleNoOperationFound(operation);
			case IN_PROGRESS:
				return handleInProgress(operation);
			case ALREADY_FINISHED_WITH_EXCEPTION:
				handleFinishedWithException(operation);
				break;
			case ALREADY_FINISHED:
				return handleAlreadyFinished(operation);
			case UNKNOWN:
			default:
				throw new IllegalArgumentException("Unknown reason!");
		}

		return null;
	}

	private <T> T handleNoOperationFound(final IdempotentOperation<T> operation) {
		String message = "No operation found for the provided id :" + operation.getId();
		log.warn(message);
		throw new WebApplicationException(message, Response.Status.NOT_FOUND.getStatusCode());
	}

	private <T> T handleUnableToFinish() {
		String errorMessage = "Failed to update request data to FINISHED status. Concurrent update possible.";
		log.warn(errorMessage);
		throw new WebApplicationException(errorMessage, Response.Status.INTERNAL_SERVER_ERROR);
	}

	protected <T> T handleInProgress(final IdempotentOperation<T> operation) {
		log.debug("The operation is in progress. Returning the predefined response ...");
		return operation.getInProgressResponse();
	}

	protected <T> void handleFinishedWithException(final IdempotentOperation<T> operation) {
		log.debug("The operation has finished already. An exception was thrown during its execution. Returning saved exception ...");

		String json = operationManager.getJsonContent(operation.getId());

		try {
			throw jsonUtils.exceptionFromJson(json);
		}
		catch (ClassNotFoundException e) {
			throw jsonUtils.mapException(e);
		}
	}

	protected <T> T handleAlreadyFinished(final IdempotentOperation<T> operation) {
		log.debug("The operation has finished already. Returning saved response ...");

		String json = operationManager.getJsonContent(operation.getId());

		log.debug("Deserializing with class {} {}", operation.getResponseClass().getName(), json);

		return jsonUtils.fromJson(json, operation.getResponseClass());
	}

}
