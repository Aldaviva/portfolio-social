package com.aldaviva.portfolio.social;

import com.aldaviva.portfolio.social.config.ApplicationConfig;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.testng.annotations.BeforeMethod;

@ContextConfiguration(classes = { ApplicationConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
public abstract class AbstractInjectedTest {

	@BeforeMethod
	public void before() throws Exception {
		new TestContextManager(getClass()).prepareTestInstance(this);
	}
}
