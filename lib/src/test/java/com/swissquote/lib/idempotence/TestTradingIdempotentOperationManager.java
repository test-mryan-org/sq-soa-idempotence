package com.swissquote.lib.idempotence;

import java.util.Date;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.swissquote.bank.test.support.AbstractDaoTestWithRollback;
import com.swissquote.bank.test.support.UnitDao;
import com.swissquote.crm.da.data.soa.ClientSoaRequest;
import com.swissquote.foundation.soa.idempotence.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.IdempotentOperationManager;
import com.swissquote.foundation.soa.idempotence.Result;

@ContextConfiguration(locations = {"classpath:/test-applicationContext.xml"})
public class TestTradingIdempotentOperationManager extends AbstractDaoTestWithRollback {
	@Autowired
	private IdempotentOperationManager operationManager;

	private IdempotentOperation<BasicResponse> operation;

	@Autowired
	HibernateTransactionManager transactionManager;

	private ClientSoaRequest clientSoaRequest;

	@Before
	public void before() throws Exception {
		setUnitDao(new UnitDao(transactionManager.getSessionFactory()));
		cleanUp();
		transactionTemplate =
				new TransactionTemplate(transactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
		setTransactionTemplate(transactionTemplate);
		setUnitDao(new UnitDao(transactionManager.getSessionFactory()));
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				clientSoaRequest = new ClientSoaRequest();
				clientSoaRequest.setStatus(ClientSoaRequest.RequestStatus.IN_PROGRESS.getStatusCode());
				clientSoaRequest.setDateIn(new Date());
				getUnitDao().save(clientSoaRequest);
			}
		});

		operation = new IdempotentOperation<BasicResponse>() {
			@Override
			public Long getRequestId() {
				return clientSoaRequest.getRequestId();
			}

			@Override
			public Object getRequestPayload() {
				return "test";
			}

			@Override
			public BasicResponse getInProgressResponse() {
				return null;
			}

			@Override
			public BasicResponse process() {
				return null;
			}

			@Override
			public Class<?> getResponseClass() {
				return BasicResponse.class;
			}
		};
	}

	@Test
	public void validResult() {
		BasicResponse dataResponse = new BasicResponse();

		operationManager.markAsFinished(operation, dataResponse);

		Result result = operationManager.markAsInProgress(operation);

		Assert.assertEquals(Result.Status.ALREADY_FINISHED, result.getReason());
	}

	@Test
	public void validCreationOfRequestId() {
		Long requestId = operationManager.createNewOperation();
		Assert.assertNotNull(requestId);
		requestId = operationManager.createNewOperation("test");
		Assert.assertNotNull(requestId);
		Long requestId2 = operationManager.createNewOperation("test");
		Assert.assertThat(requestId, Matchers.is(requestId2));
	}

	@Test
	public void exception() {
		operationManager.markAsFailed(operation, new RuntimeException("SimulatedException"));
		Result result = operationManager.markAsInProgress(operation);
		Assert.assertEquals(Result.Status.ALREADY_FINISHED_WITH_EXCEPTION, result.getReason());
	}

	@Override
	public Class[] getClassesToDelete() {
		return new Class[] {ClientSoaRequest.class};
	}
}
