package com.swissquote.foundation.soa.idempotence.server;

import java.util.Objects;

public final class Result {
	private final boolean success;
	private final Reason reason;

	public enum Reason {
		NO_OPERATION_FOUND, //
		IN_PROGRESS, //
		ALREADY_FINISHED_WITH_EXCEPTION, //
		ALREADY_FINISHED, //
		UNEXPECTED_STATUS, //
		UNKNOWN
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

	private Result(final boolean success, Reason reason) {
		this.success = success;
		this.reason = reason;
	}

	public static Result success() {
		return new Result(true, null);
	}

	public static Result fail(final Reason reason) {
		return new Result(false, reason);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Result result = (Result) o;
		return success == result.success && reason == result.reason;
	}

	@Override
	public int hashCode() {
		return Objects.hash(success, reason);
	}

	@Override
	public String toString() {
		return "Result{" + "success=" + success + ", reason=" + reason + '}';
	}
}