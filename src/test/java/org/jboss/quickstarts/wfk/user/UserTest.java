/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.quickstarts.wfk.user;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.contact.UniqueEmailException;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>A suite of tests, run with {@link org.jboss.arquillian Arquillian} to test the JAX-RS endpoint for
 * User creation functionality
 * (see {@link UserRestService#createUser(User)}).<p/>
 *
 * @author Jiaxuan Xu
 * @see UserRestService
 */
@RunWith(Arquillian.class)
public class UserTest {

	/**
	 * <p>Compiles an Archive using Shrinkwrap, containing those external dependencies necessary to run the tests.</p>
	 *
	 * <p>Note: This code will be needed at the start of each Arquillian test, but should not need to be edited, except
	 * to pass *.class values to .addClasses(...) which are appropriate to the functionality you are trying to test.</p>
	 *
	 * @return Micro test war to be deployed and executed.
	 */
	@Deployment
	public static Archive<?> createTestArchive() {
		// This is currently not well tested. If you run into issues, comment line 67 (the contents of 'resolve') and
		// uncomment 65. This will build our war with all dependencies instead.
		File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
				//                .importRuntimeAndTestDependencies()
				.resolve("io.swagger:swagger-jaxrs:1.5.16").withTransitivity().asFile();

		return ShrinkWrap.create(WebArchive.class, "test.war")
				.addPackages(true, "org.jboss.quickstarts.wfk")
				.addAsLibraries(libs)
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				.addAsWebInfResource("arquillian-ds.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@Inject
	UserRestService userRestService;

	@Inject
	UserService userService;

	@Inject
	@Named("logger")
	Logger log;

	@Test
	@InSequence(1)
	public void testRegister() throws Exception {
		User user = createUserInstance("Jack Doe", "jack@mailinator.com", "04475368829");
		Response response = userRestService.createUser(user);

		assertEquals("Unexpected response status", 201, response.getStatus());
		log.info(" New user was persisted and returned status " + response.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(2)
	public void testInvalidRegister() {
		User user = createUserInstance("", "", "");

		try {
			userRestService.createUser(user);
			fail("Expected a RestServiceException to be thrown");
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
			assertEquals("Unexpected response body", 3, e.getReasons().size());
			log.info("Invalid user register attempt failed with return code " + e.getStatus());
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(3)
	public void testDuplicateEmail() throws Exception {
		// Register an initial user
		User user = createUserInstance("Jiaxuan Xu", "jane@mailinator.com", "04475368829");
		userRestService.createUser(user);

		// Register a different user with the same email
		User anotherUser = createUserInstance("John Doe", "jane@mailinator.com", "04475368829");

		try {
			userRestService.createUser(anotherUser);
			fail("Expected a RestServiceException to be thrown");
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
			assertTrue("Unexecpted error. Should be Unique email violation", e.getCause() instanceof UniqueEmailException);
			assertEquals("Unexpected response body", 1, e.getReasons().size());
			log.info("Duplicate user register attempt failed with return code " + e.getStatus());
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(4)
	public void testGetUserList() {
		try {
			Response response = userRestService.retrieveAllUsers();
			List<User> users = response.readEntity(new GenericType<List<User>>() {
			});
			assertTrue("User size is zero", users.size() > 0);
		} catch (Exception e) {
			log.info("Unexcepted error When get user list ");
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(5)
	public void testDeleteUser() {
		// Register an initial user
		User user = createUserInstance("Jiaxuan Xu", "jane1@mailinator.com", "04475368829");
		userRestService.createUser(user);

		Long id = user.getId();
		assertTrue("User has created successfully", id != null);

		// Now delete this user
		userRestService.deleteUser(id);

		// Check if it is deleted successfully
		try {
			userService.findById(id);
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.NOT_FOUND, e.getStatus());
		}
	}

	/**
	 * <p>A utility method to construct a {@link User User} object for use in
	 * testing. This object is not persisted.</p>
	 *
	 * @param name  The  name of the User being created
	 * @param email The email address of the User being created
	 * @param phone The phone number of the User being created
	 * @return The User object create
	 */
	private User createUserInstance(String name, String email, String phone) {
		User user = new User();
		user.setName(name);
		user.setEmail(email);
		user.setPhonenumber(phone);
		return user;
	}

}
