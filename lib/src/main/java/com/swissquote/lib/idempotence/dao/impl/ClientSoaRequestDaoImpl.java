package com.swissquote.lib.idempotence.dao.impl;

import com.swissquote.lib.idempotence.dao.ClientSoaRequestDao;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.swissquote.bank.da.dao.support.AbstractEntityValidatedDao;
import com.swissquote.crm.da.data.soa.ClientSoaRequest;
import com.swissquote.foundation.time.TimeVariant;

/**
 * (c) Swissquote 11.11.14
 * @author mpamingl ttchougo
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ClientSoaRequestDaoImpl extends AbstractEntityValidatedDao implements ClientSoaRequestDao {

	@Override
	public Long create() {
		ClientSoaRequest clientRequest = new ClientSoaRequest();
		clientRequest.setStatus(ClientSoaRequest.RequestStatus.NEW.getStatusCode());
		clientRequest.setDateIn(TimeVariant.newDate());
		getSession().save(clientRequest);
		return clientRequest.getRequestId();
	}

	@Override
	public Long createWithExternalRequestId(String externalRequestId) {
		ClientSoaRequest clientRequest = new ClientSoaRequest();
		clientRequest.setStatus(ClientSoaRequest.RequestStatus.NEW.getStatusCode());
		clientRequest.setDateIn(TimeVariant.newDate());
		clientRequest.setExternalRequestId(externalRequestId);
		getSession().save(clientRequest);
		return clientRequest.getRequestId();
	}

	@Override
	public ClientSoaRequest get(Long requestId) {
		return (ClientSoaRequest) getSession().get(ClientSoaRequest.class, requestId);
	}

	@Override
	public ClientSoaRequest getRequestIdByExternalRequestId(String externalRequestId) {
		return (ClientSoaRequest) getSession().createCriteria(ClientSoaRequest.class)
				.add(Restrictions.eq("externalRequestId", externalRequestId)).uniqueResult();
	}

	@Override
	public int updateToFinished(Long requestId, ClientSoaRequest.RequestStatus expectedStatus, ClientSoaRequest.RequestStatus newStatus,
			String gsonResponse) {
		Query query = getSession().createQuery("update  "//
				+ ClientSoaRequest.class.getName() //
				+ " set status = :status, " //
				+ " result = :result " //
				+ " where requestId = :requestId AND status = :statusExpected");

		// where clause
		query.setParameter("requestId", requestId);
		query.setParameter("statusExpected", expectedStatus.getStatusCode());

		// fields that are set
		query.setParameter("status", newStatus.getStatusCode());
		query.setParameter("result", gsonResponse);

		return query.executeUpdate();
	}

	@Override
	public int updateToInProgress(Long requestId, ClientSoaRequest.RequestStatus expectedStatus, ClientSoaRequest.RequestStatus newStatus) {
		Query query = getSession().createQuery("update  " //
				+ ClientSoaRequest.class.getName() //
				+ " set status = :status " //
				+ " where requestId = :requestId AND status= :statusExpected");

		// where clause
		query.setParameter("requestId", requestId);
		query.setParameter("statusExpected", expectedStatus.getStatusCode());

		// fields that are set
		query.setParameter("status", newStatus.getStatusCode());

		return query.executeUpdate();
	}

	@Override
	protected Class<?>[] usedEntities() {
		return new Class[] {ClientSoaRequest.class};
	}
}
