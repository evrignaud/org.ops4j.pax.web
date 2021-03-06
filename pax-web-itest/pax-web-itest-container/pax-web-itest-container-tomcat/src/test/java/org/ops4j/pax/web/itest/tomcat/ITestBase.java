package org.ops4j.pax.web.itest.tomcat;

import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.CoreOptions.workingDirectory;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.exam.MavenUtils.asInProject;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.web.itest.base.TestConfiguration.*;


import javax.inject.Inject;

import org.apache.catalina.Globals;
import org.junit.After;
import org.junit.Before;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.web.itest.base.HttpTestClient;
import org.ops4j.pax.web.itest.base.ServletListenerImpl;
import org.ops4j.pax.web.itest.base.WaitCondition;
import org.ops4j.pax.web.itest.base.WebListenerImpl;
import org.ops4j.pax.web.service.spi.ServletListener;
import org.ops4j.pax.web.service.spi.WebListener;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExamReactorStrategy(PerClass.class)
public class ITestBase {

	protected static final String WEB_CONTEXT_PATH = "Web-ContextPath";
	protected static final String WEB_CONNECTORS = "Web-Connectors";
	protected static final String WEB_VIRTUAL_HOSTS = "Web-VirtualHosts";
	protected static final String WEB_BUNDLE = "webbundle:";

	protected static final String COVERAGE_COMMAND = "coverage.command";


	protected static final String REALM_NAME = "realm.properties";

	private static final Logger LOG = LoggerFactory.getLogger(ITestBase.class);

	@Inject
	protected BundleContext bundleContext;

	protected WebListener webListener;

	protected ServletListener servletListener;

	protected HttpTestClient testClient;

	public Option[] baseConfigure() {
		return options(
				workingDirectory("target/paxexam/"),
				cleanCaches(true),
				junitBundles(),

				frameworkProperty("felix.bootdelegation.implicit").value(
						"false"),
				// frameworkProperty("felix.log.level").value("4"),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("INFO"),
				systemProperty("org.osgi.service.http.hostname").value(
						"127.0.0.1"),
				systemProperty("org.osgi.service.http.port").value("8282"),
				systemProperty("java.protocol.handler.pkgs").value(
						"org.ops4j.pax.url"),
				systemProperty("org.ops4j.pax.url.war.importPaxLoggingPackages")
						.value("true"),
				systemProperty("org.ops4j.pax.web.log.ncsa.enabled").value(
						"true"),
				systemProperty("org.ops4j.pax.web.log.ncsa.directory").value(
						"logs"),
				systemProperty("ProjectVersion").value(
						PAX_WEB_VERSION),
				systemProperty("org.ops4j.pax.url.mvn.certificateCheck").value("false"),
				systemPackages(
						"javax.xml.namespace;version=1.0.0"
						),
				systemProperty("javax.servlet.context.tempdir").value("target"),
				systemProperty(Globals.CATALINA_BASE_PROP).value("target"),
										
				mavenBundle().groupId("org.ops4j.pax.web.itest")
						.artifactId("pax-web-itest-base").versionAsInProject(),

				paxWebBundles(),
						
				logbackBundles(),
				
                mavenBundle("commons-codec", "commons-codec").version(
						asInProject()),
                wrappedBundle(mavenBundle("org.apache.httpcomponents",
                        "httpcore").version(asInProject())),
                wrappedBundle(mavenBundle("org.apache.httpcomponents",
                        "httpcore-nio").version(asInProject())),
                wrappedBundle(mavenBundle("org.apache.httpcomponents",
                        "httpmime").version(asInProject())),
                wrappedBundle(mavenBundle("org.apache.httpcomponents",
                        "httpclient").version(asInProject())),
                wrappedBundle(mavenBundle("org.apache.httpcomponents",
                        "httpasyncclient").version(asInProject()))
                );
	}

	public Option[] configureBaseWithServlet() {
		return combine(
				baseConfigure(),
				mavenBundle().groupId("javax.servlet")
				.artifactId("javax.servlet-api").versionAsInProject());
	}

	public Option[] configureTomcat() {
		return combine(
				configureBaseWithServlet(),
				mavenBundle().groupId("org.ops4j.pax.web")
				.artifactId("pax-web-runtime").version(asInProject()),
				mavenBundle().groupId("org.ops4j.pax.web")
						.artifactId("pax-web-tomcat").version(asInProject()),
			
				mavenBundle().groupId("javax.annotation").artifactId("javax.annotation-api").versionAsInProject(),

				mavenBundle().groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.tomcat-embed-core")
						.version(asInProject()),

				mavenBundle()
						.groupId("org.ops4j.pax.tipi")
						.artifactId(
								"org.ops4j.pax.tipi.tomcat-embed-logging-juli")
						.version(asInProject()),

				mavenBundle().groupId("org.ops4j.pax.tipi")
						.artifactId("org.ops4j.pax.tipi.tomcat-embed-websocket")
						.version(asInProject()),

				mavenBundle().groupId("org.apache.servicemix.specs")
						.artifactId("org.apache.servicemix.specs.saaj-api-1.3")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.servicemix.specs")
						.artifactId("org.apache.servicemix.specs.jaxb-api-2.2")
						.version(asInProject()),

				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-jaxws_2.2_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-jaxrpc_1.1_spec")
						.version(asInProject()),

				mavenBundle().groupId("javax.websocket")
						.artifactId("javax.websocket-api")
						.versionAsInProject(),
						
				mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-jta_1.1_spec").versionAsInProject(),		
				
				mavenBundle()
						.groupId("org.apache.servicemix.specs")
						.artifactId(
								"org.apache.servicemix.specs.jsr303-api-1.0.0")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-activation_1.1_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-stax-api_1.2_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-ejb_3.1_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-jpa_2.0_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-javamail_1.4_spec")
						.version(asInProject()),
				mavenBundle().groupId("org.apache.geronimo.specs")
						.artifactId("geronimo-osgi-registry")
						.version(asInProject())
						);
	}
	
	@Before
	public void setUpITestBase() throws Exception {
		testClient = new HttpTestClient();
	}

	@After
	public void tearDownITestBase() throws Exception {
	    testClient.close();
	    testClient = null;
	}
	
	private boolean isEquinox() {
		return "equinox".equals(System.getProperty("pax.exam.framework"));
	}


	protected void initWebListener() {
		webListener = new WebListenerImpl();
		bundleContext.registerService(WebListener.class, webListener, null);
	}

	protected void initServletListener() {
		initServletListener(null);
	}

	protected void initServletListener(String servletName) {
		if (servletName == null)
			servletListener = new ServletListenerImpl();
		else
			servletListener = new ServletListenerImpl(servletName);
		bundleContext.registerService(ServletListener.class, servletListener,
				null);
	}

	protected void waitForWebListener() throws InterruptedException {
		new WaitCondition("webapp startup") {
			@Override
			protected boolean isFulfilled() {
				return ((WebListenerImpl) webListener).gotEvent();
			}
		}.waitForCondition(); 
	}

	protected void waitForServletListener() throws InterruptedException {
		new WaitCondition("servlet startup") {
			@Override
			protected boolean isFulfilled() {
				return ((ServletListenerImpl) servletListener).gotEvent();
			}
		}.waitForCondition(); 
	}

	protected void waitForServer(final String path) throws InterruptedException {
		new WaitCondition("server") {
			@Override
			protected boolean isFulfilled() throws Exception {
				return testClient.checkServer(path);
			}
		}.waitForCondition();
	}

	protected Bundle installAndStartBundle(String bundlePath)
			throws BundleException, InterruptedException {
		final Bundle bundle = bundleContext.installBundle(bundlePath);
		bundle.start();
		new WaitCondition("bundle startup") {
			@Override
			protected boolean isFulfilled() {
				return bundle.getState() == Bundle.ACTIVE;
			}
		}.waitForCondition();
		return bundle;
	}

}
