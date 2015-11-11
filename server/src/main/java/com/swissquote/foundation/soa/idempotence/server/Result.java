package com.swissquote.foundation.soa.idempotence.server;

public class Result {
	private final boolean success;
	private final Reason reason;

	public static enum Reason {
		NO_OPERATION_FOUND, //
		IN_PROGRESS, //
		ALREADY_FINISHED_WITH_EXCEPTION, //
		ALREADY_FINISHED, //
		UNEXPECTED_STATUS, //
		UNKNOWN;
	}

	public boolean succeeded() {
		return success;
	}

	public boolean failed() {
		return !success;
	}

	public Reason getReason() {
		return reason;
	}

	public Result(final boolean success) {
		this(true, null);
	}

	public Result(final boolean success, Reason reason) {
		this.success = success;
		this.reason = reason;
	}

	public static Result success() {
		return new Result(true);
	}

	public static Result fail(final Reason reason) {
		return new Result(false, reason);
	}
}
