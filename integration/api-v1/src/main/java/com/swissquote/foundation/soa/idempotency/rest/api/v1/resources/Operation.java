package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

public class Operation {
	private String description;
	private boolean throwBusinessCheckedExcetion = false;
	private boolean throwBusinessUncheckedExcetion = false;
	private boolean throwClientException = false;
	private boolean throwWebApplicationException = false;
	private boolean throwGenericThrowable = false;
	private boolean addExecutionIndex = true;
	private Long sleepMilis = null;

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

	public boolean isAddExecutionIndex() {
		return addExecutionIndex;
	}

	public Operation setAddExecutionIndex(boolean addExecutionIndex) {
		this.addExecutionIndex = addExecutionIndex;
		return this;
	}

	public Long getSleepMilis() {
		return sleepMilis;
	}

	public Operation setSleepMilis(Long sleepMilis) {
		this.sleepMilis = sleepMilis;
		return this;
	}

	public boolean isThrowWebApplicationException() {
		return throwWebApplicationException;
	}

	public Operation setThrowWebApplicationException(boolean throwWebApplicationException) {
		this.throwWebApplicationException = throwWebApplicationException;
		return this;
	}
}
