package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

public class Operation {
	private String description;
	private boolean throwBusinessCheckedExcetion = false;
	private boolean throwBusinessUncheckedExcetion = false;
	private boolean throwClientException = false;
	private boolean throwGenericThrowable = false;

	public boolean isThrowBusinessCheckedExcetion() {
		return throwBusinessCheckedExcetion;
	}

	public String getDescription() {
		return description;
	}

	public Operation setDescription(String description) {
		this.description = description;
		return this;
	}

	public Operation setThrowBusinessCheckedExcetion(boolean throwBusinessCheckedExcetion) {
		this.throwBusinessCheckedExcetion = throwBusinessCheckedExcetion;
		return this;
	}

	public boolean isThrowBusinessUncheckedExcetion() {
		return throwBusinessUncheckedExcetion;
	}

	public Operation setThrowBusinessUncheckedExcetion(boolean throwBusinessUncheckedExcetion) {
		this.throwBusinessUncheckedExcetion = throwBusinessUncheckedExcetion;
		return this;
	}

	public boolean isThrowClientException() {
		return throwClientException;
	}

	public Operation setThrowClientException(boolean throwClientException) {
		this.throwClientException = throwClientException;
		return this;
	}

	public boolean isThrowGenericThrowable() {
		return throwGenericThrowable;
	}

	public Operation setThrowGenericThrowable(boolean throwGenericThrowable) {
		this.throwGenericThrowable = throwGenericThrowable;
		return this;
	}
}
