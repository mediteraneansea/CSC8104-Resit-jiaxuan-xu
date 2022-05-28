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

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * <p>This Service assumes the Control responsibility in the ECB pattern.</p>
 *
 * <p>The validation is done here so that it may be used by other Boundary Resources. Other Business Logic would go here
 * as well.</p>
 *
 * <p>There are no access modifiers on the methods, making them 'package' scope.  They should only be accessed by a
 * Boundary / Web Service class with public methods.</p>
 *
 *
 * @author Jiaxuan Xu
 * @see RestaurantValidator
 * @see RestaurantRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class RestaurantService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private RestaurantValidator validator;

    @Inject
    private RestaurantRepository crud;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public RestaurantService() {
    }

    /**
     * <p>Returns a List of all persisted {@link Restaurant} objects.<p/>
     *
     * @return List of Restaurant objects
     */
    List<Restaurant> findAll() {
        return crud.findAll();
    }

    /**
     * <p>Returns a single Restaurant object, specified by a Long id.<p/>
     *
     * @param id The id field of the Restaurant to be returned
     * @return The Restaurant with the specified id
     */
    public Restaurant findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Writes the provided Restaurant object to the application database.<p/>
     *
     * <p>Validates the data in the provided Restaurant object using a {@link RestaurantValidator} object.<p/>
     *
     * @param restaurant The Restaurant object to be written to the database using a {@link RestaurantRepository} object
     * @return The Restaurant object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Restaurant create(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
        log.info("RestaurantService.create() - Creating " + restaurant.getName());
        
        // Check to make sure the data fits with the parameters in the Restaurant model and passes validation.
        validator.validateRestaurant(restaurant);

        // Write the restaurant to the database.
        return crud.create(restaurant);
    }
}
