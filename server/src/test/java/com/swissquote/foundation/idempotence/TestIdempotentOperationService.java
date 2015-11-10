package com.swissquote.foundation.idempotence;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestIdempotentOperationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestIdempotentOperationService.class);

	private Server server;

	public void before() throws Exception {
		server = new Server(8080);

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/sq-soa-idempotence-integration-soa");

		String warFilePath = System.getProperty("war.file.path");
		LOGGER.info("War file path param: " + warFilePath);

		webapp.setWar(warFilePath);
		server.setHandler(webapp);

		server.start();
		server.join();
	}

	public void test() {
		Assert.fail("Magic fail");
	}
}
