package com.swissquote.foundation.soa.idempotency.api.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.swissquote.foundation.soa.idempotency.api.v1.entities.Operation;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.OperationResponse;

@Path("idem-service")
public interface IdempotentOperationResource {

	@POST
	@Path("/operation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	Long createNewOperation();

	@POST
	@Path("/operation/simple")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	OperationResponse processSimpleOperation(Operation operation);

	@POST
	@Path("/operation/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	OperationResponse processIdempotentOperation(@PathParam("operationId") Long operationId, Operation operation);

}
