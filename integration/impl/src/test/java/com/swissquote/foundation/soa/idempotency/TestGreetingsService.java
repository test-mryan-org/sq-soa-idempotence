package com.swissquote.foundation.soa.idempotency;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Example unit test
 */
public class TestGreetingsService {

	@Test
	public void test_greeting() {
		String greeting = new GreetingsService("coucou").greetings("David");
		assertThat(greeting, is("coucou David!"));
	}

}