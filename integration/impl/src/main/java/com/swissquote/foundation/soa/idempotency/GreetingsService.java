package com.swissquote.foundation.soa.idempotency;

import java.beans.ConstructorProperties;

public class GreetingsService {

	private final String greetingsSentence;

	@ConstructorProperties("greetingsSentence")
	public GreetingsService(String greetingsSentence) {
		this.greetingsSentence = greetingsSentence;
	}

	public String greetings(String who) {
		return new StringBuilder(greetingsSentence).append(' ').append(who).append('!').toString();
	}
}
