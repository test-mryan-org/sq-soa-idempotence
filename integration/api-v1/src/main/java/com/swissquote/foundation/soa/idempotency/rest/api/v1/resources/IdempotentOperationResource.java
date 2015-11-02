package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Module.NAME + "/someService")
public interface IdempotentOperationResource {

	@POST
	@Path("/operation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	Long createNewOperation();

	Long prepareOperation(OperationSetupRequest request);

}
