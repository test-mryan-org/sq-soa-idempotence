package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.swissquote.foundation.soa.support.api.exceptions.BusinessCheckedException;

@Path(Module.NAME + "/someService")
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
	OperationResponse processSimpleOperation(Operation operation) throws BusinessCheckedException;

	@POST
	@Path("/operation/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	OperationResponse processIdempotentOperation(@PathParam("operationId") Long operationId, Operation operation)
			throws BusinessCheckedException;

	@POST
	@Path("/operationWithGsonPolymorphic/idempotent/{operationId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	OperationResponseWithGsonPolymorphic processIdempotentOperation2(@PathParam("operationId") Long operationId, Operation operation)
			throws BusinessCheckedException;
}
