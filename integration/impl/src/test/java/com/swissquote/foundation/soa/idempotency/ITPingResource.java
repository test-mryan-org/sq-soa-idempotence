package com.swissquote.foundation.soa.idempotency;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.entities.ClientId;
import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.PingResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-applicationContext.xml"})
public class ITPingResource {

	@Autowired
	private PingResource pingResource;

	@Test
	public void test() {
		Assert.assertEquals("Pong 200341!", pingResource.ping(new ClientId("200341")));
	}
}
