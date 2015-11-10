package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.core.InjectParam;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Module;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Operation;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationRequest;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.OperationResponse;
import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

@Path(Module.NAME + "/someService")
public class IdempotentOperationResourceImpl implements IdempotentOperationResource {

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
		return OperationProcessorImpl.instance().process(operation);
	}

	@Override
	@POST
	@Path("/operation/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	public OperationResponse processIdempotentOperation(@PathParam("operationId") final Long operationId, final Operation operation)
			throws BusinessCheckedException {
		IdempotentOperation<OperationResponse> idempotentOperation = new IdempotentOperation<OperationResponse>() {
			@Override
			public Long getRequestId() {
				return operationId;
			}

			@Override
			public Object getRequestPayload() {
				return null;
			}

			@Override
			public OperationResponse getInProgressResponse() {
				return OperationResponse.builder().inProgress(true).build();
			}

			@Override
			public OperationResponse process() throws BusinessCheckedException {
				return OperationProcessorImpl.instance().process(operation);
			}

			@Override
			public Class<?> getResponseClass() {
				return OperationResponse.class;
			}
		};
		return idempotentOperationService.process(idempotentOperation);
	}

	public OperationResponse process(final OperationRequest data) throws BusinessCheckedException {

		IdempotentOperation<OperationResponse> operation = new IdempotentOperation<OperationResponse>() {
			@Override
			public Long getRequestId() {
				return data.getRequestId();
			}

			@Override
			public Object getRequestPayload() {
				return data;
			}

			@Override
			public OperationResponse getInProgressResponse() {
				return OperationResponse.builder().inProgress(true).build();
			}

			@Override
			public OperationResponse process() throws BusinessCheckedException {
				return OperationResponse.builder().inProgress(false).build();
			}

			@Override
			public Class<?> getResponseClass() {
				return OperationResponse.class;
			}
		};
		return idempotentOperationService.process(operation);
	}

	public IdempotentOperationService getIdempotentOperationService() {
		return idempotentOperationService;
	}

	public void setIdempotentOperationService(final IdempotentOperationService idempotentOperationService) {
		this.idempotentOperationService = idempotentOperationService;
	}

}
