package com.swissquote.foundation.soa.idempotence;

public class Result {

	private final Status status;
	private RuntimeException exception;
	private Object result;

	public static enum Status {
		SUCCESS(true), //
		NO_OPERATION_FOUND(false), //
		IN_PROGRESS(false), //
		ALREADY_FINISHED_WITH_EXCEPTION(false), //
		ALREADY_FINISHED(false), //
		UNEXPECTED_STATUS(false), //
		UNKNOWN(false);

		private boolean success;

		Status(final boolean success) {
			this.success = success;
		}

		public boolean getSuccess() {
			return success;
		}

		public boolean failed() {
			return !success;
		}
	}

	public Result(final Status status) {
		super();
		this.status = status;
	}

	public Result(final Status status, final RuntimeException exception) {
		super();
		this.status = status;
		this.exception = exception;
	}

	public Result(final Status status, final Object result) {
		super();
		this.status = status;
		this.result = result;
	}

	public boolean failed() {
		return status.failed();
	}

	public Status getReason() {
		return status;
	}

	public RuntimeException getException() {
		return exception;
	}

	public Object getResult() {
		return result;
	}

}
