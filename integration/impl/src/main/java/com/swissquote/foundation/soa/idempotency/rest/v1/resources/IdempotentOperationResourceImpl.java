package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.core.InjectParam;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.InProgressOperationResponse;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Module;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponseWithGsonPolymorphic;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

@Path(Module.NAME + "/someService")
public class IdempotentOperationResourceImpl implements IdempotentOperationResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(IdempotentOperationResourceImpl.class);

	@InjectParam
	private IdempotentOperationService idempotentOperationService;

	@POST
	@Path("/operation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	@Override
	public Long createNewOperation() {
		return idempotentOperationService.createNewOperation();
	}

	@Override
	@POST
	@Path("/operation/simple")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	public OperationResponse processSimpleOperation(final Operation operation) throws BusinessCheckedException {
		return new OperationProcessorImpl().process(operation);
	}

	@Override
	@POST
	@Path("/operation/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	public OperationResponse processIdempotentOperation(@PathParam("operationId") final Long operationId, final Operation operation)
			throws BusinessCheckedException {
		IdempotentOperation<OperationResponse, BusinessCheckedException> idempotentOperation =
				new IdempotentOperation<OperationResponse, BusinessCheckedException>() {
					@Override
					public Long getId() {
						return operationId;
					}

					@Override
					public Object getRequestPayload() {
						return operation;
					}

					@Override
					public OperationResponse getInProgressResponse() {
						return OperationResponse.builder().inProgress(true).build();
					}

					@Override
					public OperationResponse process() throws BusinessCheckedException {
						return new OperationProcessorImpl().process(operation);
					}

					@Override
					public Class<?> getResponseClass() {
						return OperationResponse.class;
					}
				};
		return idempotentOperationService.process(idempotentOperation);
	}

	@Override
	@POST
	@Path("/operationWithGsonPolymorphic/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	public OperationResponseWithGsonPolymorphic processIdempotentOperation2(final @PathParam("operationId") Long operationId,
			final Operation operation)
					throws BusinessCheckedException {

		LOGGER.info("Received request with ID = {}", operationId);
		IdempotentOperation<OperationResponseWithGsonPolymorphic, BusinessCheckedException> idempotentOperation =
				new IdempotentOperation<OperationResponseWithGsonPolymorphic, BusinessCheckedException>() {
					@Override
					public Long getId() {
						return operationId;
					}

					@Override
					public Object getRequestPayload() {
						return operation;
					}

					@Override
					public OperationResponseWithGsonPolymorphic getInProgressResponse() {
						return new InProgressOperationResponse();
					}

					@Override
					public OperationResponseWithGsonPolymorphic process() throws BusinessCheckedException {
						return new OperationProcessor2Impl().process(operation);
					}

					@Override
					public Class<?> getResponseClass() {
						return OperationResponseWithGsonPolymorphic.class;
					}
				};
		return idempotentOperationService.process(idempotentOperation);
	}

	public IdempotentOperationService getIdempotentOperationService() {
		return idempotentOperationService;
	}

	public void setIdempotentOperationService(final IdempotentOperationService idempotentOperationService) {
		this.idempotentOperationService = idempotentOperationService;
	}

}
