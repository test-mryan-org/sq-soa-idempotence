package com.swissquote.foundation.soa.idempotence.server;

public interface IdempotentOperationManager {
	Long createNewOperation();

	Result markAsInProgress(Long operationId, String requestAsJson);

	Result markAsFinished(Long operationId, String resultAsJson);

	Result markAsFailed(Long operationId, String exceptionAsJson);

	String getJsonContent(Long operationId);
}
