package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.sun.jersey.api.core.InjectParam;
import com.swissquote.foundation.soa.idempotency.GreetingsService;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.entities.ClientId;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.Module;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.PingResource;
import com.swissquote.foundation.soa.support.api.exceptions.ServiceException;

@Path(Module.NAME + "/ping")
public class PingResourceImpl implements PingResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(PingResourceImpl.class);

	@InjectParam
	private GreetingsService service;

	public PingResourceImpl() {
		this.service = null;
	}

	@SuppressWarnings("boxing")
	@GET
	@Path("{client}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("ROLE_PING")
	@Override
	public String ping(@PathParam("client") ClientId client) {
		LOGGER.info("Pinging client " + client);

		if ("fail".equals(client.getClient())) {
			ServiceException exception = new ServiceException("Not possible");

			exception.setHttpCode(HttpStatus.BAD_REQUEST.value());
			throw exception;
		}
		return service.greetings(client.getClient());
	}

	public GreetingsService getService() {
		return service;
	}

	public void setService(GreetingsService service) {
		this.service = service;
	}
}