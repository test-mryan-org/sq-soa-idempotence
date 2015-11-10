package com.swissquote.foundation.soa.idempotence.server;

public class Result {
	private final Status status;

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

	private Result(final Status status) {
		super();
		this.status = status;
	}

	public boolean failed() {
		return status.failed();
	}

	public Status getReason() {
		return status;
	}

	public static Result forStatus(final Status status) {
		return new Result(status);
	}
}
