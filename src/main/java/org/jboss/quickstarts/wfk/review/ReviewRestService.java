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

import org.jboss.quickstarts.wfk.area.InvalidAreaCodeException;
import org.jboss.quickstarts.wfk.restaurant.Restaurant;
import org.jboss.quickstarts.wfk.user.User;
import org.jboss.quickstarts.wfk.user.UserService;
import org.jboss.quickstarts.wfk.restaurant.RestaurantService;
import org.jboss.quickstarts.wfk.util.RestServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * <p>This class produces a RESTful service exposing the functionality of {@link ReviewService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/reviews/*</p>
 *
 * @author Jiaxuan Xu
 * @see ReviewService
 * @see Response
 */
@Path("/reviews")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/reviews", description = "Operations about reviews")
@Stateless
public class ReviewRestService {

	@Inject
	private @Named("logger")
	Logger log;

	@Inject
	private ReviewService service;

	@Inject
	private UserService userService;

	@Inject
	private RestaurantService restaurantService;


	/**
	 * <p>Return all the Reviews.  They are sorted chronologically by reviewDate.</p>
	 *
	 * <p>The url may optionally include query parameters specifying a Review's userId</p>
	 *
	 * <p>Examples: <pre>GET api/reviews?userId=1</pre></p>
	 *
	 * @return A Response containing a list of Reviews
	 */
	@GET
	@Path("/getByUserId")
	@ApiOperation(value = "Fetch all Reviews", notes = "Returns a JSON array of all stored Review objects.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Review list found")})
	public Response retrieveAllReviewsByUserId(
			@ApiParam(value = "The userId of reviews") @QueryParam("userId") Long userId) {
		//Create an empty collection to contain the intersection of Reviews to be returned
		List<Review> reviews;

		if (userId == null) {
			reviews = service.findAll();
		} else {
			reviews = service.findAllByUserId(userId);
		}
		return Response.ok(reviews).build();
	}

	/**
	 * <p>Creates a new review from the values provided. Performs validation and will return a JAX-RS response with
	 * either 201 (Resource created) or with a map of fields, and related errors.</p>
	 *
	 * @param review The Review object, constructed automatically from JSON input, to be <i>created</i> via
	 *                {@link ReviewService#create(Review)}
	 * @return A Response indicating the outcome of the create operation
	 */
	@SuppressWarnings("unused")
	@POST
	@ApiOperation(value = "Add a new Review to the database.")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "Review created successfully."),
			@ApiResponse(code = 400, message = "Invalid Review supplied in request body"),
			@ApiResponse(code = 409, message = "Review supplied in request body conflicts with an existing Review"),
			@ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")})
	public Response createReview(
			@ApiParam(value = "JSON representation of Review object to be added to the database. In the user object and restaurant " +
					"object, Only their ids are mandatory, and the other fields of them can be set to null or simply be deleted",
					required = true) Review review) {

		if (review == null) {
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
		}
		if (review.getId() != null) {
			throw new RestServiceException("ReviewId should be null", Response.Status.BAD_REQUEST);
		}

		Response.ResponseBuilder builder;
		User user;
		Restaurant restaurant;
		try {
			user = userService.findById(review.getUser().getId());
			if (user == null) throw new NullPointerException();
		} catch (Exception e) {
			throw new RestServiceException("UserId is incorrect", Response.Status.BAD_REQUEST, e);
		}
		try {
			restaurant = restaurantService.findById(review.getRestaurant().getId());
			if (restaurant == null) throw new NullPointerException();
		} catch (Exception e) {
			throw new RestServiceException("RestaurantId is incorrect", Response.Status.BAD_REQUEST, e);
		}

		try {
			// Go add the new Review.
			service.create(review);

			// Create a "Resource Created" 201 Response and pass the review back in case it is needed.
			builder = Response.status(Response.Status.CREATED).entity(review);

		} catch (ConstraintViolationException ce) {
			//Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

		} catch (UniqueResturantOnReviewDateException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("Restaurant on review date",
					"That Restaurant on the review Date is already used, please change another restaurant or " + "review date");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
		} catch (Exception e) {
			// Handle generic exceptions
			throw new RestServiceException(e);
		}

		log.info("createReview completed. Review = " + review.toString());
		return builder.build();
	}

}
