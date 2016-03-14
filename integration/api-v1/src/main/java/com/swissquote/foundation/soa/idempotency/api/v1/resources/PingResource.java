package com.swissquote.foundation.soa.idempotency.api.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.swissquote.foundation.soa.idempotency.api.v1.entities.ClientId;

@Path("ping")
public interface PingResource {

	@GET
	@Path("{client}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	String ping(@PathParam("client") ClientId client);

}
