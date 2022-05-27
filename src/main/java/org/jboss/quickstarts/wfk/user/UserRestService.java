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

import org.jboss.quickstarts.wfk.contact.UniqueEmailException;
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
 * <p>This class produces a RESTful service exposing the functionality of {@link UserService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/users/*</p>
 *
 * @author Jiaxuan Xu
 * @see UserService
 * @see Response
 */
@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/users", description = "Operations about users")
@Stateless
public class UserRestService {

	@Inject
	private @Named("logger")
	Logger log;

	@Inject
	private UserService service;

	/**
	 * <p>Return all the Users.  They are sorted alphabetically by name.</p>
	 *
	 * <p>The url may optionally include query parameters specifying a User's name</p>
	 *
	 * <p>Examples: <pre>GET api/users?name=John</pre></p>
	 *
	 * @return A Response containing a list of Users
	 */
	@GET
	@ApiOperation(value = "Fetch all Users", notes = "Returns a JSON array of all stored User objects.")
	public Response retrieveAllUsers() {
		//Create an empty collection to contain the intersection of Users to be returned
		List<User> users = service.findAll();

		return Response.ok(users).build();
	}

	/**
	 * <p>Creates a new user from the values provided. Performs validation and will return a JAX-RS response with
	 * either 201 (Resource created) or with a map of fields, and related errors.</p>
	 *
	 * @param user The User object, constructed automatically from JSON input, to be <i>created</i> via
	 *                 {@link UserService#create(User)}
	 * @return A Response indicating the outcome of the create operation
	 */
	@SuppressWarnings("unused")
	@POST
	@ApiOperation(value = "Add a new User to the database. The id of the user object is unnecessary " +
			"and can be deleted in the request body.")
	@ApiResponses(value = {
			@ApiResponse(code = 201, message = "User created successfully."),
			@ApiResponse(code = 400, message = "Invalid User supplied in request body"),
			@ApiResponse(code = 409, message = "User supplied in request body conflicts with an existing User"),
			@ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")})
	public Response createUser(@ApiParam(value = "JSON representation of User object to be added to the database. " +
			"The id of the user object is unnecessary and can be deleted in the request body.", required = true) User user) {

		if (user == null) {
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
		}
		if (user.getId() != null) {
			throw new RestServiceException("UserId should be null", Response.Status.BAD_REQUEST);
		}

		Response.ResponseBuilder builder;

		try {
			// Go add the new User.
			service.create(user);

			// Create a "Resource Created" 201 Response and pass the user back in case it is needed.
			builder = Response.status(Response.Status.CREATED).entity(user);

		} catch (ConstraintViolationException ce) {
			//Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

		} catch (UniqueEmailException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "That email is already used, please use a unique email");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
		} catch (Exception e) {
			// Handle generic exceptions
			throw new RestServiceException(e);
		}

		log.info("createUser completed. User = " + user.toString());
		return builder.build();
	}

	/**
	 * <p>Deletes a user using the ID provided. If the ID is not present then nothing can be deleted.</p>
	 *
	 * <p>Will return a JAX-RS response with either 204 NO CONTENT or with a map of fields, and related errors.</p>
	 *
	 * @param id The Long parameter value provided as the id of the User to be deleted
	 * @return A Response indicating the outcome of the delete operation
	 */
	@DELETE
	@Path("/{id:[0-9]+}")
	@ApiOperation(value = "Delete a User from the database")
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "The user has been successfully deleted"),
			@ApiResponse(code = 400, message = "Invalid User id supplied"),
			@ApiResponse(code = 404, message = "User with id not found"),
			@ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")})
	public Response deleteUser(
			@ApiParam(value = "Id of User to be deleted", allowableValues = "range[0, infinity]", required = true) @PathParam("id") long id) {

		Response.ResponseBuilder builder;

		User user = service.findById(id);
		if (user == null) {
			// Verify that the user exists. Return 404, if not present.
			throw new RestServiceException("No User with the id " + id + " was found!", Response.Status.NOT_FOUND);
		}

		try {
			service.delete(user);

			builder = Response.noContent();

		} catch (Exception e) {
			// Handle generic exceptions
			throw new RestServiceException(e);
		}
		log.info("deleteUser completed. User = " + user.toString());
		return builder.build();
	}
}
