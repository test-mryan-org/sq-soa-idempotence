package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import com.swissquote.foundation.soa.client.SqSoaWebTargetFactory;
import com.swissquote.foundation.soa.client.WebProxyBuilder;
import com.swissquote.foundation.soa.client.config.ServiceConfig;
import com.swissquote.foundation.soa.idempotence.client.ClientIdempotentOperation;
import com.swissquote.foundation.soa.idempotency.OperationNeverCompletedException;
import com.swissquote.foundation.soa.idempotency.OperationProcessorUtils;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.ClientId;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.Operation;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.OperationResponse;
import com.swissquote.foundation.soa.idempotency.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.api.v1.resources.PingResource;
import com.swissquote.foundation.soa.support.api.exceptions.ErrorDetails;

/**
 * The tests then use web invocation proxies and call the remote service via http.
 * This allows us to test the full stack of integration between the service provider and clients.
 */
@RunWith(Arquillian.class)
public class TestIntegrationService extends ArquillianTestDeployment {

	public static final String SERVICE_NAME = "sq-soa-idempotence-integration-v1";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestIntegrationService.class);

	@ArquillianResource
	public URL url;

	@Test
	public void proxy_client_interaction() throws IOException {
		assertEquals("Pong David!", getTestedService().ping(new ClientId("David")));
	}

	@Test(expected = WebApplicationException.class)
	public void proxy_client_fail_v2() {
		try {
			getTestedService().ping(new ClientId("fail"));
		}
		catch (WebApplicationException e) {
			ErrorDetails details = ErrorDetails.of(e);
			assertEquals("BUSINESS_ERROR_ID", details.getCode());
			assertEquals("value", details.getDetails().get("key"));
			throw e;
		}
	}

	private ServiceConfig buildServiceConfig() {
		String host = url.getHost();
		String url2;
		String externalForm = url.toExternalForm();
		url2 = "localhost".equals(host) ? externalForm.replace("localhost", "127.0.0.1") : externalForm.replace("127.0.0.1", "localhost");
		URI uri1 = URI.create(externalForm + "api/sq-soa-idempotence-integration-v1");
		URI uri2 = URI.create(url2 + "api/sq-soa-idempotence-integration-v1");
		return ServiceConfig.serviceConfig()
				.baseUris(new HashSet<>(Arrays.asList(uri1, uri2)))
				.connectTimeoutMillis(1000)
				.readTimeoutMillis(3000)
				.defaultAsyncExecutorPoolSize(2)
				.username("test")
				.password("pass")
				.build();
	}

	private PingResource getTestedService() {
		SqSoaWebTargetFactory factory = new SqSoaWebTargetFactory(ClientBuilder.newClient());
		WebProxyBuilder<PingResource> proxyBuilder = new WebProxyBuilder<>(PingResource.class)
				.serviceName(SERVICE_NAME)
				.config(buildServiceConfig())
				.webTargetFactory(factory);
		return proxyBuilder.build();
	}

	private IdempotentOperationResource getIdempotentOperationResource() {
		SqSoaWebTargetFactory factory = new SqSoaWebTargetFactory(ClientBuilder.newClient());
		WebProxyBuilder<IdempotentOperationResource> proxyBuilder = new WebProxyBuilder<>(IdempotentOperationResource.class)
				.serviceName(SERVICE_NAME)
				.config(buildServiceConfig())
				.webTargetFactory(factory);
		return proxyBuilder.build();
	}

	@Test
	public void theServiceCanCreateANewOperation() {
		Long operationId = getIdempotentOperationResource().createNewOperation();
		Assert.assertNotNull(operationId);
		LOGGER.info("New Operation created with id " + operationId);
	}

	@Test
	public void throwingABusinessUncheckedException() {
		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = getIdempotentOperationResource().createNewOperation();
		Operation operation2 = Operation.builder()
				.throwBusinessUncheckedException(true)
				.addExecutionIndex(false)
				.description("BusinessUncheckedException with an idempotent call")
				.build();
		Throwable t1 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t1);
		assertTrue(t1 instanceof InternalServerErrorException);

		// Re-executing the same operation a second time
		Throwable t2 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertThrowablesEqual(t1, t2);
	}

	@Test
	public void throwingAClientException() {
		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = getIdempotentOperationResource().createNewOperation();
		Operation operation2 = Operation.builder()
				.throwClientException(true)
				.addExecutionIndex(false)
				.description("ClientException with an idempotent call")
				.build();
		Throwable t1 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t1);
		assertTrue(t1 instanceof InternalServerErrorException);

		// Re-executing the same operation a second time
		Throwable t2 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertThrowablesEqual(t2, t1);
	}

	@Test
	public void throwingGenericThrowable() {
		// The same operation (that throws an exception) executed in an idempotent way
		Long operationId = getIdempotentOperationResource().createNewOperation();
		Operation operation2 = Operation.builder()
				.throwRuntimeException(true)
				.addExecutionIndex(false)
				.description("GenericThrowable with an idempotent call")
				.build();
		Throwable t1 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t1);
		assertTrue(t1 instanceof InternalServerErrorException);

		// Re-executing the same operation a second time
		Throwable t2 = getExceptionFromIdempotentCall(operationId, operation2);
		Assert.assertNotNull(t2);
		assertThrowablesEqual(t2, t1);
	}

	@Test
	public void sameRequestIsExecutedOnlyOnce() {
		IdempotentOperationResource resource = getIdempotentOperationResource();
		Long operationId = resource.createNewOperation();
		Operation operation = Operation.builder().addExecutionIndex(true).build();

		OperationResponse result1 = resource.processIdempotentOperation(operationId, operation);
		OperationResponse result2 = resource.processIdempotentOperation(operationId, operation);

		assertEquals(result1, result2);
		Assert.assertTrue(result1.getExecutionIndex() > 0);
	}

	@Test
	public void sameRequestIsExecutedTwiceIfTheIdIsNotTheSame() {
		IdempotentOperationResource resource = getIdempotentOperationResource();
		Long operationId1 = resource.createNewOperation();
		Operation operation = Operation.builder().addExecutionIndex(true).build();

		OperationResponse result1 = resource.processIdempotentOperation(operationId1, operation);
		Long operationId2 = resource.createNewOperation();

		OperationResponse result2 = resource.processIdempotentOperation(operationId2, operation);
		Assert.assertNotEquals(result1, result2);
		Assert.assertNotEquals(result1.getExecutionIndex(), result2.getExecutionIndex());
	}

	@Test
	public void onlyOneClientExceptionForTheSameRequestId() {
		IdempotentOperationResource resource = getIdempotentOperationResource();
		Long operationId = resource.createNewOperation();
		Operation operation = Operation.builder().addExecutionIndex(true).throwDetailedWebAppException(true).build();

		Throwable t1 = getExceptionFromIdempotentCall(operationId, operation);
		Throwable t2 = getExceptionFromIdempotentCall(operationId, operation);

		assertThrowablesEqual(t1, t2);
		assertTheExecutionIndexIsValid(t1);
	}

	@Test
	public void operationFinishingAfterTheFirstSoaTimeOut() {
		/**
		 * The operation is prepared to take 3500ms(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.idem-service-v1.properties > read.timeout.millis=3000. This means that we will have the response from the second 
		 * machine.
		 */
		final Operation operation = Operation.builder().addExecutionIndex(true).sleepMillis(3500L).build();
		final AtomicBoolean hadIntermediaryResponse = new AtomicBoolean(false);
		final IdempotentOperationResource resource = getIdempotentOperationResource();
		OperationResponse result = createClientIdempotentOperation(10, 100, operation, resource, hadIntermediaryResponse).execute();

		Assert.assertNotNull(result);
		Assert.assertFalse(result.isInProgress());
		Assert.assertTrue(hadIntermediaryResponse.get());
	}

	@Test(expected = OperationNeverCompletedException.class)
	public void operationFinishingAfterSoaTimeOut() {
		/**
		 * The operation is prepared to take 20s(a sleep) while the soa configuration is to perform a switch to a different machine after
		 * sq.soa.client.idem-service-v1.properties > read.timeout.millis=3000. This means that from the first system we will get a timeout, the
		 * sq-soa layer will switch to the second machine from it it will get a "inProgress" response. It will keep trying (9 more times with a
		 * sleep of 1000ms before retrying) and in the end it will execute the code specified in handleNeverCompleted;
		 */
		final Operation operation = Operation.builder().addExecutionIndex(true).sleepMillis(20000L).build();
		final IdempotentOperationResource resource = getIdempotentOperationResource();
		OperationResponse result = createClientIdempotentOperation(10, 1000, operation, resource, new AtomicBoolean(false)).execute();
		Assert.assertNotNull(result);
	}

	@Test
	public void stressTest() throws InterruptedException {
		ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
		final IdempotentOperationResource resource = getIdempotentOperationResource();
		for (int i = 0; i < 20; i++) {
			taskExecutor.submit(new Runnable() {

				@Override
				public void run() {
					final Operation operation = Operation.builder().addExecutionIndex(true).sleepMillis(3100L).build();
					final AtomicBoolean hadIntermediaryResponse = new AtomicBoolean(false);
					try {
						OperationResponse result = new ClientIdempotentOperation<OperationResponse>(10, 100) {

							@Override
							public Long createNew() {
								return resource.createNewOperation();
							}

							@Override
							public OperationResponse attemptExecution(Long operationId) {
								return resource.processIdempotentOperation(operationId, operation);
							}

							@Override
							public boolean isComplete(OperationResponse result) {
								hadIntermediaryResponse.set(true);
								return !result.isInProgress();
							}

							@Override
							public OperationResponse handleNeverCompleted(OperationResponse result) {
								throw new RuntimeException("operation did not finish");
							}
						}.execute();
						Assert.assertNotNull(result);
						Assert.assertTrue(hadIntermediaryResponse.get());
						Assert.assertFalse(result.isInProgress());

					}
					catch (RuntimeException e) {
						//
					}
				}
			});
		}
		taskExecutor.shutdown();
		taskExecutor.awaitTermination(100, TimeUnit.HOURS);

	}

	private void assertTheExecutionIndexIsValid(final Throwable t1) {
		assertTrue(t1 instanceof WebApplicationException);
		ErrorDetails details = ErrorDetails.of((WebApplicationException) t1);

		String index = details.getDetails().get(OperationProcessorUtils.VARIABLE_NAME);
		int executionIndex = Integer.parseInt(index);
		Assert.assertTrue(executionIndex > 0);

	}

	private void assertThrowablesEqual(Throwable t1, Throwable t2) {
		Assert.assertNotNull(t1);
		Assert.assertNotNull(t2);
		assertEquals(t1.getClass(), t2.getClass());
		if (t1 instanceof HystrixRuntimeException) {
			return;
		}
		if (t1 instanceof WebApplicationException && t2 instanceof WebApplicationException) {
			WebApplicationException wex1 = (WebApplicationException) t1;
			WebApplicationException wex2 = (WebApplicationException) t2;
			assertEquals(wex1.getResponse().getStatus(), wex2.getResponse().getStatus());

		}
		assertEquals(String.format("Expecting [%s] but got [%s]", t1.getMessage(), t2.getMessage()), t1.getMessage(), t2.getMessage());
	}

	private Throwable getExceptionFromIdempotentCall(Long id, Operation operation) {
		try {
			getIdempotentOperationResource().processIdempotentOperation(id, operation);
			return null;
		}
		catch (final Throwable e) {
			return e;
		}
	}

	private ClientIdempotentOperation<OperationResponse> createClientIdempotentOperation(final int noOfCalls, final int sleepMillis,
			final Operation operation, final IdempotentOperationResource resource, final AtomicBoolean hadIntermediaryResponse) {
		return new ClientIdempotentOperation<OperationResponse>(noOfCalls, sleepMillis) {

			@Override
			public Long createNew() {
				return resource.createNewOperation();
			}

			@Override
			public OperationResponse attemptExecution(Long operationId) {
				return resource.processIdempotentOperation(operationId, operation);
			}

			@Override
			public boolean isComplete(OperationResponse r) {
				hadIntermediaryResponse.set(true);
				return !r.isInProgress();
			}

			@Override
			public OperationResponse handleNeverCompleted(OperationResponse r) {
				throw new OperationNeverCompletedException("operation did not finish");
			}
		};
	}
}