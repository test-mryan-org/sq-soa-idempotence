package com.swissquote.lib.idempotence;

public class BasicResponse {

	private boolean success;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "SignupResponse{"
				+ ", success=" + success + '}';
	}
}
