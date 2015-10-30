package com.swissquote.lib.idempotence.dao;

import com.swissquote.crm.da.data.soa.ClientSoaRequest;
import com.swissquote.crm.da.data.soa.ClientSoaRequest;

/**
 * (c) Swissquote 11.11.14
 * @author mpamingl ttchougo
 */
public interface ClientSoaRequestDao {
	Long create();

	Long createWithExternalRequestId(String externalRequestId);

	ClientSoaRequest get(Long requestId);

	ClientSoaRequest getRequestIdByExternalRequestId(String externalRequestId);

	int updateToFinished(Long requestId, ClientSoaRequest.RequestStatus expectedStatus, ClientSoaRequest.RequestStatus newStatus,
			String gsonResponse);

	int updateToInProgress(Long requestId, ClientSoaRequest.RequestStatus expectedStatus, ClientSoaRequest.RequestStatus inProgress);
}
