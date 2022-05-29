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
package org.jboss.quickstarts.wfk.restaurant;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
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
 * Restaurant creation functionality
 * (see {@link RestaurantRestService#createRestaurant(Restaurant)}).<p/>
 *
 * @author Jiaxuan Xu
 * @see RestaurantRestService
 */
@RunWith(Arquillian.class)
public class RestaurantTest {

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
	RestaurantRestService restaurantRestService;

	@Inject
	@Named("logger")
	Logger log;

	@Test
	@InSequence(1)
	public void testRegister() throws Exception {
		Restaurant restaurant = createRestaurantInstance("AB16HOG", 7);
		Response response = restaurantRestService.createRestaurant(restaurant);

		assertEquals("Unexpected response status", Response.Status.CREATED.getStatusCode(), response.getStatus());
		log.info(" New restaurant was persisted and returned status " + response.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(2)
	public void testInvalidRegister() {
		Restaurant restaurant = createRestaurantInstance("123456789", 0);

		try {
			restaurantRestService.createRestaurant(restaurant);
			fail("Expected a RestServiceException to be thrown");
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
			assertEquals("Unexpected response body", 2, e.getReasons().size());
			log.info("Invalid restaurant register attempt failed with return code " + e.getStatus());
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(3)
	public void testDuplicatePhonenumber() throws Exception {
		// Register an initial restaurant
		Restaurant restaurant = createRestaurantInstance("AB16HOO", 7);
		restaurantRestService.createRestaurant(restaurant);

		// Register a different restaurant with the same phonenumber
		Restaurant anotherRestaurant = createRestaurantInstance("AB16HOO", 5);

		try {
			restaurantRestService.createRestaurant(anotherRestaurant);
			fail("Expected a RestServiceException to be thrown");
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.CONFLICT, e.getStatus());
			assertTrue("Unexecpted error. Should be Unique phonenumber violation", e.getCause() instanceof UniquePhonenumberException);
			assertEquals("Unexpected response body", 1, e.getReasons().size());
			log.info("Duplicate restaurant register attempt failed with return code " + e.getStatus());
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(4)
	public void testGetRestaurantList() {
		try {
			Response response = restaurantRestService.retrieveAllRestaurants();
			List<Restaurant> restaurants = response.readEntity(new GenericType<List<Restaurant>>() {
			});
			assertTrue("Restaurant size is zero", restaurants.size() > 0);
		} catch (Exception e) {
			log.info("Unexcepted error When get restaurant list ");
		}
	}


	/**
	 * <p>A utility method to construct a {@link Restaurant Restaurant} object for use in
	 * testing. This object is not persisted.</p>
	 *
	 * @param phonenumber The phonenumber of the Restaurant being created
	 * @param seats        The seats of the Restaurant being created
	 * @return The Restaurant object create
	 */
	private Restaurant createRestaurantInstance(String phonenumber, int seats) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(phonenumber);
		restaurant.setPhonenumber("01212300011");
		restaurant.setPostcode("ABC123");
		return restaurant;
	}

}
