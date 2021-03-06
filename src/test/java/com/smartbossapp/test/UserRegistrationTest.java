package com.smartbossapp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.smartbossapp.data.UserData;
import com.smartbossapp.model.User;
import com.smartbossapp.rest.UserService;
import com.smartbossapp.service.UserRegistration;
import com.smartbossapp.util.Resources;

/**
 * Uses Arquilian to test the JAX-RS processing class for User registration.
 * 
 * @author balunasj
 */
@RunWith(Arquillian.class)
public class UserRegistrationTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addClasses(User.class, UserService.class, UserData.class, UserRegistration.class, Resources.class)
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				.addAsWebInfResource("arquillian-ds.xml").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	UserService UserRegistration;

	@Inject
	Logger log;

	@Test
	@InSequence(1)
	public void testRegister() throws Exception {
		final User User = createUserInstance("Jack Doe", "jack@mailinator.com", "2125551234");
		final Response response = UserRegistration.createUser(User);

		assertEquals("Unexpected response status", 200, response.getStatus());
		log.info(" New User was persisted and returned status " + response.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(2)
	public void testInvalidRegister() throws Exception {
		final User User = createUserInstance("", "", "");
		final Response response = UserRegistration.createUser(User);

		assertEquals("Unexpected response status", 400, response.getStatus());
		assertNotNull("response.getEntity() should not null", response.getEntity());
		assertEquals("Unexpected response.getEntity(). It contains " + response.getEntity(), 3,
				((Map<String, String>) response.getEntity()).size());
		log.info("Invalid User register attempt failed with return code " + response.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(3)
	public void testDuplicateEmail() throws Exception {
		// Register an initial user
		final User User = createUserInstance("Jane Doe", "jane@mailinator.com", "2125551234");
		UserRegistration.createUser(User);

		// Register a different user with the same email
		final User anotherUser = createUserInstance("John Doe", "jane@mailinator.com", "2133551234");
		final Response response = UserRegistration.createUser(anotherUser);

		assertEquals("Unexpected response status", 409, response.getStatus());
		assertNotNull("response.getEntity() should not null", response.getEntity());
		assertEquals("Unexpected response.getEntity(). It contains" + response.getEntity(), 1,
				((Map<String, String>) response.getEntity()).size());
		log.info("Duplicate User register attempt failed with return code " + response.getStatus());
	}

	private User createUserInstance(final String firstname, final String email, final String lastname) {
		final User User = new User();
		User.setEmail(email);
		User.setFirstName(firstname);
		User.setLastName(lastname);
		return User;
	}
}
