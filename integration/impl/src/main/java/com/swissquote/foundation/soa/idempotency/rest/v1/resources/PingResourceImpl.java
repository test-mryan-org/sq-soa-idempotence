package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.Collections;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.swissquote.foundation.soa.idempotency.GreetingsService;
import com.swissquote.foundation.soa.idempotency.api.v1.entities.ClientId;
import com.swissquote.foundation.soa.idempotency.api.v1.resources.PingResource;
import com.swissquote.foundation.soa.support.api.exceptions.DetailedWebApplicationException;

@Path("ping")
public class PingResourceImpl implements PingResource {

	@Inject
	private GreetingsService service;

	@GET
	@Path("{client}")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed("ROLE_PING")
	@Override
	public String ping(@PathParam("client") ClientId client) {
		if ("fail".equals(client.getClient())) {
			throw new DetailedWebApplicationException("Not possible", BAD_REQUEST.getStatusCode(), "BUSINESS_ERROR_ID",
					Collections.singletonMap("key", "value"));
		}
		return service.greetings(client.getClient());
	}
}