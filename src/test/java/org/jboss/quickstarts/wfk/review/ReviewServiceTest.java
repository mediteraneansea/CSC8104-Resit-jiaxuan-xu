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
package org.jboss.quickstarts.wfk.review;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.quickstarts.wfk.restaurant.Restaurant;
import org.jboss.quickstarts.wfk.restaurant.RestaurantService;
import org.jboss.quickstarts.wfk.user.User;
import org.jboss.quickstarts.wfk.user.UserRestService;
import org.jboss.quickstarts.wfk.restaurant.RestaurantRestService;
import org.jboss.quickstarts.wfk.user.UserService;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>A suite of tests, run with {@link org.jboss.arquillian Arquillian} to test the JAX-RS endpoint for
 * User creation functionality
 * (see {@link ReviewRestService#createReview(Review)}).<p/>
 *
 * @author Jiaxuan Xu
 * @see ReviewRestService
 */
@RunWith(Arquillian.class)
public class ReviewServiceTest {

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
	ReviewRestService reviewRestService;

	@Inject
	UserRestService userRestService;

	@Inject
	RestaurantRestService restaurantRestService;

	@Inject
	UserService userService;

	@Inject
	RestaurantService restaurantService;

	@Inject
	ReviewService reviewService;

	@Inject
	@Named("logger")
	Logger log;

	@Test
	@InSequence(1)
	public void testReview() throws Exception {
		// Create a new user
		User user = createUserInstance("Jack Doeo", "jack1@mailinator.com", "04475368829");
		Response userResponse = userRestService.createUser(user);
		assertEquals("Unexcept error happened", Response.Status.CREATED.getStatusCode(), userResponse.getStatus());

		// Create a new restaurant
		Restaurant restaurant = createRestaurantInstance("Smiths","01234567890", "AB16HO");
		Response restaurantResponse = restaurantRestService.createRestaurant(restaurant);
		assertEquals("Unexcept error happened", Response.Status.CREATED.getStatusCode(), restaurantResponse.getStatus());

		Review review = createReviewInstance(user.getId(), restaurant.getId(), "This is an excellent restaurant", 5);
		Response response = reviewRestService.createReview(review);

		assertEquals("Unexpected response status", Response.Status.CREATED.getStatusCode(), response.getStatus());
		log.info(" New Review was persisted and returned status " + response.getStatus());
	}

	@SuppressWarnings("unchecked")
	@Test
	@InSequence(2)
	public void testInvalidReview() {
		// Retrieve an existing user
		User user = createUserInstance("Jack Doe", "jack2@mailinator.com", "04475368829");
		Response userResponse = userRestService.createUser(user);

		// Create a new restaurant
		Restaurant restaurant = createRestaurantInstance("Smiths","01234567891", "AB16HO");
		Response restaurantResponse = restaurantRestService.createRestaurant(restaurant);

		Review review = createReviewInstance(user.getId(), restaurant.getId(), "This is an excellent restaurant", 6);
		try {
			Response response = reviewRestService.createReview(review);
			fail("Expected a RestServiceException to be thrown");
		} catch (RestServiceException e) {
			assertEquals("Unexpected response status", Response.Status.BAD_REQUEST, e.getStatus());
			assertEquals("Unexpected response body", 1, e.getReasons().size());
			log.info("Invalid user register attempt failed with return code " + e.getStatus());
		}
	}


	@SuppressWarnings("unchecked")
	@Test
	@InSequence(4)
	public void testGetReviewList() {
		try {
			// Retrieve an existing user
			User user = createUserInstance("Jack Doie", "jack3@mailinator.com", "04475368829");
			Response userResponse = userRestService.createUser(user);

			// Create a new restaurant
			Restaurant restaurant = createRestaurantInstance("Smiths","01234567890", "AB16HO");
			Response restaurantResponse = restaurantRestService.createRestaurant(restaurant);

			Review review = createReviewInstance(user.getId(), restaurant.getId(), "This is an excellent restaurant", 5);
			reviewRestService.createReview(review);

			Response response = reviewRestService.retrieveAllReviewsByUserId(null);
			List<Review> reviews = response.readEntity(new GenericType<List<Review>>() {
			});
			assertTrue("Reviews size is zero", reviews.size() > 0);
		} catch (Exception e) {
			log.info("Unexcepted error When get review list ");
		}
	}

	/**
	 * <p>A utility method to construct a {@link User User} object for use in
	 * testing. This object is not persisted.</p>
	 *
	 * @param userId  the user id who books this order
	 * @param restaurantId      the restaurant id of this review
	 * @param content           The content of review
	 * @param rating            The score of review
	 * @return The Review object create
	 */
	private Review createReviewInstance(Long userId, Long restaurantId, String content, int rating) {
		Review review = new Review();
		User user = new User();
		user.setId(userId);
		review.setUser(user);
		Restaurant restaurant = new Restaurant();
		restaurant.setId(restaurantId);
		review.setRestaurant(restaurant);
		review.setRating(rating);
		review.setReview(content);
		return review;
	}

	private Date getRandomFutureDate() {
		int days = (int) (Math.random() * 100) + 100;
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, days);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
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

	/**
	 * <p>A utility method to construct a {@link Restaurant Restaurant} object for use in
	 * testing. This object is not persisted.</p>
	 */
	private Restaurant createRestaurantInstance(String name, String phonenumber, String postcode) {
		Restaurant restaurant = new Restaurant();
		restaurant.setName(name);
		restaurant.setPhonenumber(phonenumber);
		restaurant.setPostcode(postcode);
		return restaurant;
	}
}
