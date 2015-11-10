package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.Maps;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.server.Result;

public class TestIdempotentOperationManager implements IdempotentOperationManager {
	private ConcurrentMap<Long, Operation> map = Maps.newConcurrentMap();

	@Override
	public Long createNewOperation() {
		Operation operation = new Operation();
		map.put(operation.getId(), operation);
		return operation.getId();
	}

	@Override
	public <T> Result markAsInProgress(final IdempotentOperation<T> idempotentOperation) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			return Result.forStatus(Result.Status.NO_OPERATION_FOUND);
		}

		if (Operation.Status.NEW.equals(operation.getStatus()) || Operation.Status.ERROR.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.IN_PROGRESS);
			return Result.forStatus(Result.Status.SUCCESS);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			return Result.forStatus(Result.Status.IN_PROGRESS);
		}

		if (Operation.Status.FINISHED_WITH_SUCCESS.equals(operation.getStatus())) {
			return Result.forStatus(Result.Status.ALREADY_FINISHED);
		}

		if (Operation.Status.FINISHED_WITH_EXCEPTION.equals(operation.getStatus())) {
			return Result.forStatus(Result.Status.ALREADY_FINISHED_WITH_EXCEPTION);
		}

		return Result.forStatus(Result.Status.UNEXPECTED_STATUS);
	}

	@Override
	public <T> Result markAsFinished(final IdempotentOperation<T> idempotentOperation, T result) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			return Result.forStatus(Result.Status.NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.FINISHED_WITH_SUCCESS);
			operation.setResult(result);
			return Result.forStatus(Result.Status.SUCCESS);
		}

		return Result.forStatus(Result.Status.UNEXPECTED_STATUS);

	}

	@Override
	public <T> Result markAsFailed(IdempotentOperation<T> idempotentOperation, Exception t) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			return Result.forStatus(Result.Status.NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.FINISHED_WITH_EXCEPTION);
			operation.setException(t);
			return Result.forStatus(Result.Status.SUCCESS);
		}

		return Result.forStatus(Result.Status.UNEXPECTED_STATUS);
	}

	@Override
	public <T> Result markAsError(IdempotentOperation<T> idempotentOperation) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			return Result.forStatus(Result.Status.NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.ERROR);
			return Result.forStatus(Result.Status.SUCCESS);
		}

		return Result.forStatus(Result.Status.UNEXPECTED_STATUS);
	}

	@Override
	public <T> T getResult(IdempotentOperation<T> idempotentOperation) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			throw new IllegalStateException("No operation found for requestId() " + idempotentOperation.getRequestId());
		}
		return (T) operation.getResult();
	}

	@Override
	public <T> Exception getException(IdempotentOperation<T> idempotentOperation) {
		Operation operation = map.get(idempotentOperation.getRequestId());
		if (operation == null) {
			throw new IllegalStateException("No operation found for requestId() " + idempotentOperation.getRequestId());
		}
		return operation.getException();
	}

	static class Operation {
		private static final AtomicLong index = new AtomicLong(0);
		private Long id;
		private Status status;
		private Object result;
		private Exception exception;

		enum Status {
			NEW,
			IN_PROGRESS,
			FINISHED_WITH_SUCCESS,
			FINISHED_WITH_EXCEPTION,
			ERROR;
		}

		public Operation() {
			this.id = index.incrementAndGet();
			this.status = Status.NEW;
		}

		public Long getId() {
			return id;
		}

		public Status getStatus() {
			return status;
		}

		public void setStatus(final Status status) {
			this.status = status;
		}

		public Object getResult() {
			return result;
		}

		public void setResult(final Object result) {
			this.result = result;
		}

		public Exception getException() {
			return exception;
		}

		public void setException(final Exception throwable) {
			this.exception = throwable;
		}

	}

}
