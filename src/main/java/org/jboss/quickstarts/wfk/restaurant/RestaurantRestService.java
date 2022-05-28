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

import org.jboss.quickstarts.wfk.area.InvalidAreaCodeException;
import org.jboss.quickstarts.wfk.util.RestServiceException;
import org.jboss.resteasy.annotations.cache.Cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
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
 * <p>This class produces a RESTful service exposing the functionality of {@link RestaurantService}.</p>
 *
 * <p>The Path annotation defines this as a REST Web Service using JAX-RS.</p>
 *
 * <p>By placing the Consumes and Produces annotations at the class level the methods all default to JSON.  However, they
 * can be overriden by adding the Consumes or Produces annotations to the individual methods.</p>
 *
 * <p>It is Stateless to "inform the container that this RESTful web service should also be treated as an EJB and allow
 * transaction demarcation when accessing the database." - Antonio Goncalves</p>
 *
 * <p>The full path for accessing endpoints defined herein is: api/restaurants/*</p>
 * 
 * @author Jiaxuan Xu
 * @see RestaurantService
 * @see Response
 */
@Path("/restaurants")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "/restaurants", description = "Operations about restaurants")
@Stateless
public class RestaurantRestService {
    @Inject
    private @Named("logger") Logger log;
    
    @Inject
    private RestaurantService service;

    /**
     * <p>Return all the Restaurants.  They are sorted alphabetically by a seats number.</p>
     *
     * <p>The url may optionally include query parameters specifying a Restaurant's seats</p>
     *
     * <p>Examples: <pre>GET api/restaurants?seats=4</pre></p>
     *
     * @return A Response containing a list of Restaurants
     */
    @GET
    @ApiOperation(value = "Fetch all Restaurants", notes = "Returns a JSON array of all stored Restaurant objects.")
    public Response retrieveAllRestaurants() {
        //Create an empty collection to contain the intersection of Restaurants to be returned
        List<Restaurant> restaurants = service.findAll();

        return Response.ok(restaurants).build();
    }

    /**
     * <p>Creates a new restaurant from the values provided. Performs validation and will return a JAX-RS response with
     * either 201 (Resource created) or with a map of fields, and related errors.</p>
     *
     * @param restaurant The Restaurant object, constructed automatically from JSON input, to be <i>created</i> via
     * {@link RestaurantService#create(Restaurant)}
     * @return A Response indicating the outcome of the create operation
     */
    @SuppressWarnings("unused")
    @POST
    @ApiOperation(value = "Add a new Restaurant to the database")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Restaurant created successfully."),
            @ApiResponse(code = 400, message = "Invalid Restaurant supplied in request body"),
            @ApiResponse(code = 409, message = "Restaurant supplied in request body conflicts with an existing Restaurant"),
            @ApiResponse(code = 500, message = "An unexpected error occurred whilst processing the request")
    })
    public Response createRestaurant(
            @ApiParam(value = "JSON representation of Restaurant object to be added to the database", required = true) Restaurant restaurant) {


        if (restaurant == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        if (restaurant.getId() != null) {
            throw new RestServiceException("RestaurantId should be null", Response.Status.BAD_REQUEST);
        }
        Response.ResponseBuilder builder;

        try {
            // Go add the new Restaurant.
            service.create(restaurant);

            // Create a "Resource Created" 201 Response and pass the restaurant back in case it is needed.
            builder = Response.status(Response.Status.CREATED).entity(restaurant);


        } catch (ConstraintViolationException ce) {
            //Handle bean validation issues
            Map<String, String> responseObj = new HashMap<>();

            for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
                responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
            }
            throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

        } catch (UniquePhonenumberException e) {
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("phonenumber", "That phonenumber is already used, please use a unique phonenumber");
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (Exception e) {
            // Handle generic exceptions
            throw new RestServiceException(e);
        }

        log.info("createRestaurant completed. Restaurant = " + restaurant.toString());
        return builder.build();
    }

}
