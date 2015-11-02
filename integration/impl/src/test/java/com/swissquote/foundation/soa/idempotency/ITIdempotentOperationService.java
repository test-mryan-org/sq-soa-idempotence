package com.swissquote.foundation.soa.idempotency;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.entities.ClientId;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.PingResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-applicationContext.xml"})
public class ITIdempotentOperationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ITIdempotentOperationService.class);

	@Autowired
	private PingResource pingResource;

	@Test
	public void test() {

		LOGGER.info("------------>><< Integration tests");

		pingResource.ping(new ClientId("200341"));
	}
}
