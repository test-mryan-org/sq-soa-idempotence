package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import static com.swissquote.foundation.soa.idempotence.server.Result.Reason.ALREADY_FINISHED;
import static com.swissquote.foundation.soa.idempotence.server.Result.Reason.ALREADY_FINISHED_WITH_EXCEPTION;
import static com.swissquote.foundation.soa.idempotence.server.Result.Reason.IN_PROGRESS;
import static com.swissquote.foundation.soa.idempotence.server.Result.Reason.NO_OPERATION_FOUND;
import static com.swissquote.foundation.soa.idempotence.server.Result.Reason.UNEXPECTED_STATUS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.server.Result;

public class TestIdempotentOperationManager implements IdempotentOperationManager {
	private ConcurrentMap<Long, Operation> map = new ConcurrentHashMap<Long, TestIdempotentOperationManager.Operation>();

	@Override
	public Long createNewOperation() {
		Operation operation = new Operation();
		map.put(operation.getId(), operation);
		return operation.getId();
	}

	@Override
	public Result markAsInProgress(final Long operationId, final String requestAsJson) {
		Operation operation = map.get(operationId);
		if (operation == null) {
			return Result.fail(NO_OPERATION_FOUND);
		}

		if (Operation.Status.NEW.equals(operation.getStatus()) || Operation.Status.ERROR.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.IN_PROGRESS);
			return Result.success();
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			return Result.fail(IN_PROGRESS);
		}

		if (Operation.Status.FINISHED_WITH_SUCCESS.equals(operation.getStatus())) {
			return Result.fail(ALREADY_FINISHED);
		}

		if (Operation.Status.FINISHED_WITH_EXCEPTION.equals(operation.getStatus())) {
			return Result.fail(ALREADY_FINISHED_WITH_EXCEPTION);
		}

		return Result.fail(UNEXPECTED_STATUS);
	}

	@Override
	public Result markAsFinished(final Long operationId, final String resultAsJson) {
		Operation operation = map.get(operationId);
		if (operation == null) {
			return Result.fail(NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.FINISHED_WITH_SUCCESS);
			operation.setContent(resultAsJson);
			return Result.success();
		}

		return Result.fail(UNEXPECTED_STATUS);

	}

	@Override
	public Result markAsFailed(final Long operationId, final String exceptionAsJson) {
		Operation operation = map.get(operationId);
		if (operation == null) {
			return Result.fail(NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.FINISHED_WITH_EXCEPTION);
			operation.setContent(exceptionAsJson);
			return Result.success();
		}

		return Result.fail(UNEXPECTED_STATUS);
	}

	@Override
	public Result markAsError(final Long operationId) {
		Operation operation = map.get(operationId);
		if (operation == null) {
			return Result.fail(NO_OPERATION_FOUND);
		}

		if (Operation.Status.IN_PROGRESS.equals(operation.getStatus())) {
			operation.setStatus(Operation.Status.ERROR);
			return Result.success();
		}

		return Result.fail(UNEXPECTED_STATUS);
	}

	@Override
	public String getJsonContent(final Long operationId) {
		Operation operation = map.get(operationId);
		if (operation == null) {
			throw new IllegalStateException("No operation found for requestId() " + operationId);
		}
		return operation.getContent();
	}

	static class Operation {
		private static final AtomicLong index = new AtomicLong(0);
		private Long id;
		private Status status;
		private String content;

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

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

	}

}
