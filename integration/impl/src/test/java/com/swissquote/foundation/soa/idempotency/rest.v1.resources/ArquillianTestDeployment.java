package com.swissquote.foundation.soa.idempotency.rest.v1.resources;

import java.io.File;
import java.io.IOException;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.ClassRule;
import org.junit.contrib.java.lang.system.ProvideSystemProperty;

/**
 * deploys and configures the service implementation in an embedded container.
 * The deployed application can be used by all test classes
 */
@ArquillianSuiteDeployment
public class ArquillianTestDeployment {

	@ClassRule
	public static final ProvideSystemProperty itConfigPath = new ProvideSystemProperty("sq.itconfig.path", "src/test/resources/it_config");

	public static final String WEBAPP_NAME = "sq-soa-idempotence-integration-soa";

	@Deployment(testable = false)
	public static Archive<?> buildTestApplicationWar() throws IOException {
		// We are using an embedded container, so the webapp classpath will be managed by the test execution environment
		// all we need is the web.xml in the WAR.
		return ShrinkWrap.create(WebArchive.class, WEBAPP_NAME + ".war").setWebXML(new File("./src/main/webapp/WEB-INF/web.xml"));
	}
}
