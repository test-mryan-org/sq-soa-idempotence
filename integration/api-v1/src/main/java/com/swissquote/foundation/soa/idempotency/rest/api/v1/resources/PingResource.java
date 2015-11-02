package com.swissquote.foundation.soa.idempotency.rest.api.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.entities.ClientId;

@Path(Module.NAME + "/ping")
public interface PingResource {

	@GET
	@Path("{client}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("ROLE_PING")
	String ping(@PathParam("client") ClientId client);

}
