package com.swissquote.foundation.soa.idempotence.impl;

import com.swissquote.crm.da.data.soa.ClientSoaRequest;
import com.swissquote.foundation.soa.idempotence.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.Result;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;
import com.swissquote.foundation.soa.support.platform.gson.GsonConfig;
import com.swissquote.lib.idempotence.dao.ClientSoaRequestDao;

@SuppressWarnings("PMD.CyclomaticComplexity")
public class IdempotentOperationManagerImpl implements IdempotentOperationManager {

	private final ClientSoaRequestDao clientSoaRequestDao;

	private final GsonConfig gsonConfig = new GsonConfig();

	public IdempotentOperationManagerImpl(final ClientSoaRequestDao clientSoaRequestDao) {
		this.clientSoaRequestDao = clientSoaRequestDao;
	}

	@Override
	public Long createNewOperation() {
		return clientSoaRequestDao.create();
	}

	@Override
	public Long createNewOperation(String externalRequestId) {
		ClientSoaRequest clientSoaRequest = clientSoaRequestDao.getRequestIdByExternalRequestId(externalRequestId);
		if (clientSoaRequest == null) {
			return clientSoaRequestDao.createWithExternalRequestId(externalRequestId);
		}
		return clientSoaRequest.getRequestId();

	}

	@Override
	public <T> Result markAsInProgress(final IdempotentOperation<T> operation) {
		boolean processingStarted = markAsInProgress(operation.getRequestId());
		if (processingStarted) {
			return new Result(Result.Status.SUCCESS);
		}

		ClientSoaRequest existingRequest = clientSoaRequestDao.get(operation.getRequestId());

		if (existingRequest == null) {
			return new Result(Result.Status.NO_OPERATION_FOUND);
		}

		if (ClientSoaRequest.RequestStatus.IN_PROGRESS.equals(existingRequest.getRequestStatus())) {
			return new Result(Result.Status.IN_PROGRESS);
		}

		if (ClientSoaRequest.RequestStatus.FINISHED.equals(existingRequest.getRequestStatus())) {
			return handleAlreadyFinished(operation, existingRequest);
		}

		if (ClientSoaRequest.RequestStatus.FINISHED_WITH_EXCEPTION.equals(existingRequest.getRequestStatus())) {
			return handleAlreadyFinishedWithException(operation, existingRequest);
		}

		return new Result(Result.Status.UNKNOWN);
	}

	/**
	 * @param operation
	 * @param existingRequest
	 * @return
	 */
	protected <T> Result handleAlreadyFinished(final IdempotentOperation<T> operation, final ClientSoaRequest existingRequest) {

		Object response = gsonConfig.getGson().fromJson(existingRequest.getResult(), operation.getResponseClass());

		return new Result(Result.Status.ALREADY_FINISHED, response);
	}

	/**
	 * @param operation
	 * @param existingRequest
	 * @return
	 */
	protected <T> Result handleAlreadyFinishedWithException(final IdempotentOperation<T> operation, final ClientSoaRequest existingRequest) {

		Object response = gsonConfig.getGson().fromJson(existingRequest.getResult(), BusinessCheckedException.class);

		BusinessCheckedException exceptionInfo = (BusinessCheckedException) response;

		return new Result(Result.Status.ALREADY_FINISHED_WITH_EXCEPTION, exceptionInfo.getStackTrace());
	}

	/**
	 *
	 */
	@Override
	public <T> Result markAsFailed(final IdempotentOperation<T> operation, final Throwable t) {
		return markAsFinished(operation.getRequestId(), ClientSoaRequest.RequestStatus.FINISHED_WITH_EXCEPTION, new BusinessCheckedException(t));
	}

	/**
	 *
	 */
	@Override
	public <T> Result markAsFinished(final IdempotentOperation<T> operation, final T result) {
		return markAsFinished(operation.getRequestId(), ClientSoaRequest.RequestStatus.FINISHED, result);
	}

	/**
	 * Moves request status from NEW to IN_PROGRESS state to indicate that processing started.
	 * @return True if processing can be started and DB updated well, false if normal processing workflow can't be started.
	 */
	private boolean markAsInProgress(final Long requestId) {
		int updatedRows =
				clientSoaRequestDao.updateToInProgress(requestId, ClientSoaRequest.RequestStatus.NEW,
						ClientSoaRequest.RequestStatus.IN_PROGRESS);
		return updatedRows > 0;
	}

	protected Result markAsFinished(final Long requestId, final ClientSoaRequest.RequestStatus newStatus, final Object payload) {

		String gsonResponse = gsonConfig.getGson().toJson(payload);

		int updatedRows = clientSoaRequestDao.updateToFinished(//
				requestId, //
				ClientSoaRequest.RequestStatus.IN_PROGRESS, //
				newStatus, ////
				gsonResponse//
				);
		if (updatedRows > 0) {
			return new Result(Result.Status.SUCCESS);
		}

		ClientSoaRequest existingRequest = clientSoaRequestDao.get(requestId);

		if (existingRequest == null) {
			return new Result(Result.Status.NO_OPERATION_FOUND);
		}

		if (!ClientSoaRequest.RequestStatus.IN_PROGRESS.equals(existingRequest.getRequestStatus())) {
			return new Result(Result.Status.UNEXPECTED_STATUS);
		}

		return new Result(Result.Status.UNKNOWN);
	}

}
