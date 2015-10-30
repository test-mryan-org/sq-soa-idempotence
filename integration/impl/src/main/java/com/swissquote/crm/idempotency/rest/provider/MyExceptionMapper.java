package com.swissquote.crm.idempotency.rest.provider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MyExceptionMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException weException) {

		// get initial response
		Response response = weException.getResponse();

		// create custom error
		String error = "Something wrong happend...";

		// return the custom error
		return Response.status(response.getStatus()).header("X-Message", error).build();
	}
}