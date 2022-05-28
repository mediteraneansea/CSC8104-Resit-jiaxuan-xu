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
 * @author Jiaxuan Xu
 * @see ReviewValidator
 * @see ReviewRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class ReviewService {

	@Inject
	private @Named("logger")
	Logger log;

	@Inject
	private ReviewValidator validator;

	@Inject
	private ReviewRepository crud;

	/**
	 * <p>Create a new client which will be used for our outgoing REST client communication</p>
	 */
	public ReviewService() {
	}

	/**
	 * <p>Returns a List of all persisted {@link Review} objects, sorted by reviewDate.<p/>
	 *
	 * @return List of Review objects
	 */
	List<Review> findAll() {
		return crud.findAll();
	}

	/**
	 * <p>Returns a List of Review object, specified by a userId.<p/>
	 *
	 * @param userId The userId field of the Review to be returned
	 * @return The list of Review with the specified restaurantId
	 */
	List<Review> findAllByUserId(Long userId) {
		return crud.findAllByUserId(userId);
	}

	/**
	 * <p>Writes the provided Review object to the application database.<p/>
	 *
	 * <p>Validates the data in the provided Review object using a {@link ReviewValidator} object.<p/>
	 *
	 * @param review The Review object to be written to the database using a {@link ReviewRepository} object
	 * @return The Review object that has been successfully written to the application database
	 * @throws ConstraintViolationException, ValidationException, Exception
	 */
	public Review create(Review review) throws ConstraintViolationException, ValidationException, Exception {
		log.info("ReviewService.create() - Creating " + review.getRestaurant().getId() + " " + review.getUser().getId());

		// Check to make sure the data fits with the parameters in the Review model and passes validation.
		validator.validateReview(review);

		// Write the review to the database.
		return crud.create(review);
	}
}
