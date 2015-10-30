package com.swissquote.foundation.soa.idempotency;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.swissquote.foundation.soa.idempotency.GreetingsService;

/**
 * Example unit test
 */
public class TestGreetingsService {
	@Test
	public void test_greeting() {
		String greeting = new GreetingsService("coucou").greetings("World");
		assertThat(greeting, is("coucou World!"));
	}
}