package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.stereotype.Component;

import com.swissquote.foundation.soa.idempotence.server.IdempotentOperation;
import com.swissquote.foundation.soa.idempotence.server.IdempotentOperationService;
import com.swissquote.foundation.soa.idempotency.OperationProcessorImpl;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.Operation;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.OperationResponse;
import com.swissquote.foundation.soa.idempotency.api.v1.resources.IdempotentOperationResource;

@Path("idem-service")
@Component
public class IdempotentOperationResourceImpl implements IdempotentOperationResource {

	@Inject
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
	public OperationResponse processSimpleOperation(final Operation operation) {
		return new OperationProcessorImpl().process(operation);
	}

	@Override
	@POST
	@Path("/operation/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	public OperationResponse processIdempotentOperation(@PathParam("operationId") final Long operationId, final Operation operation) {
		IdempotentOperation<OperationResponse> idempotentOperation =
				new IdempotentOperation<OperationResponse>() {
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
					public OperationResponse process() {
						return new OperationProcessorImpl().process(operation);
					}

					@Override
					public Class<OperationResponse> getResponseClass() {
						return OperationResponse.class;
					}
				};
		return idempotentOperationService.process(idempotentOperation);
	}

}
