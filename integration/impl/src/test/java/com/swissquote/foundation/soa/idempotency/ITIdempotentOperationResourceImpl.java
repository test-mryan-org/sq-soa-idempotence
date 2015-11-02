package com.swissquote.foundation.soa.idempotency;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.swissquote.foundation.soa.idempotency.rest.api.v1.resources.IdempotentOperationResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-applicationContext.xml"})
public class ITIdempotentOperationResourceImpl {
	@Autowired
	private IdempotentOperationResource idempotentOperationResource;

	@Test
	public void test() {
		Assert.assertNotNull(idempotentOperationResource.createNewOperation());
	}
}
