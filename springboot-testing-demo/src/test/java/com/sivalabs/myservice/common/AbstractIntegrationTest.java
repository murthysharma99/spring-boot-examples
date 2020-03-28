package com.sivalabs.myservice.common;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.mockserver.client.MockServerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = { AbstractIntegrationTest.Initializer.class })
public abstract class AbstractIntegrationTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper objectMapper;

	private static PostgreSQLContainer sqlContainer;

	private static MockServerContainer mockServerContainer;

	private static final Logger log = LoggerFactory.getLogger(AbstractIntegrationTest.class);

	static {
		sqlContainer = new PostgreSQLContainer("postgres:10.7").withDatabaseName("integration-tests-db")
				.withUsername("sa").withPassword("sa");
		sqlContainer.start();

		mockServerContainer = new MockServerContainer();
		mockServerContainer.start();

	}

	protected MockServerClient mockServerClient = new MockServerClient(mockServerContainer.getContainerIpAddress(),
			mockServerContainer.getServerPort());

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			log.info("githuub.api.base-url=" + mockServerContainer.getEndpoint());
			TestPropertyValues
					.of("spring.datasource.url=" + sqlContainer.getJdbcUrl(),
							"spring.datasource.username=" + sqlContainer.getUsername(),
							"spring.datasource.password=" + sqlContainer.getPassword(),
							"githuub.api.base-url=" + mockServerContainer.getEndpoint())
					.applyTo(configurableApplicationContext.getEnvironment());
		}
	}

}
