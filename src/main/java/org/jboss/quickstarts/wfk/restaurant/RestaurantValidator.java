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


import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Restaurant objects against arbitrary requirements.</p>
 *
 * @author Jiaxuan Xu
 * @see Restaurant
 * @see RestaurantRepository
 * @see Validator
 */
public class RestaurantValidator {
    @Inject
    private Validator validator;

    @Inject
    private RestaurantRepository crud;

    /**
     * <p>Validates the given Restaurant object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing restaurant with the same phonenumber is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param restaurant The Restaurant object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If restaurant with the same phonenumber already exists
     */
    void validateRestaurant(Restaurant restaurant) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Restaurant>> violations = validator.validate(restaurant);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the phonenumber
        if (phonenumberAlreadyExists(restaurant.getPhonenumber(), restaurant.getId())) {
            throw new UniquePhonenumberException("Unique Phonenumber Violation");
        }
    }

    /**
     * <p>Checks if a restaurant with the same phonenumber address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "phonenumber")" constraint from the Restaurant class.</p>
     *
     * <p>Since Update will being using an phonenumber that is already in the database we need to make sure that it is the phonenumber
     * from the record being updated.</p>
     *
     * @param phonenumber The phonenumber to check is unique
     * @param id The user id to check the phonenumber against if it was found
     * @return boolean which represents whether the phonenumber was found, and if so if it belongs to the user with id
     */
    boolean phonenumberAlreadyExists(String phonenumber, Long id) {
        Restaurant restaurant = null;
        Restaurant restaurantWithID = null;
        try {
            restaurant = crud.findByPhonenumber(phonenumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (restaurant != null && id != null) {
            try {
                restaurantWithID = crud.findById(id);
                if (restaurantWithID != null && restaurantWithID.getName().equals(phonenumber)) {
                    restaurant = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return restaurant != null;
    }
}
