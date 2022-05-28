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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Review objects against arbitrary requirements.</p>
 *
 * @author Jiaxuan Xu
 * @see Review
 * @see ReviewRepository
 * @see Validator
 */
public class ReviewValidator {

	@Inject
	private Validator validator;

	@Inject
	private ReviewRepository crud;

	/**
	 * <p>Validates the given Review object and throws validation exceptions based on the type of error. If the error is standard
	 * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
	 *
	 *
	 * <p>If the error is caused because an existing review with the same email is registered it throws a regular validation
	 * exception so that it can be interpreted separately.</p>
	 *
	 * @param review The Review object to be validated
	 * @throws ConstraintViolationException If Bean Validation errors exist
	 * @throws ValidationException          If review with the same email already exists
	 */
	public void validateReview(Review review) throws ConstraintViolationException, ValidationException {
		// Create a bean validator and check for issues.
		Set<ConstraintViolation<Review>> violations = validator.validate(review);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}

		// Check the uniqueness of the email address
		if (restaurantWithUserIdAlreadyExists(review.getRestaurant().getId(), review.getUser().getId(), review.getId())) {
			throw new UniqueResturantOnReviewDateException("Unique Restaurant with ReviewDate Violation");
		}
	}

	/**
	 * <p>Checks if a review with the same email address is already registered. This is the only way to easily capture the
	 * "@UniqueConstraint(columnNames = "email")" constraint from the Review class.</p>
	 *
	 * <p>Since Update will being using an email that is already in the database we need to make sure that it is the email
	 * from the record being updated.</p>
	 *
	 * @param restaurantId      The restaurantId field of the Reviews
	 * @param userId The userId of the Reviews
	 * @param id          The review id to check the restaurantId and reviewDate against if it was found
	 * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
	 */
	boolean restaurantWithUserIdAlreadyExists(Long restaurantId, Long userId, Long id) {
		Review review = null;
		Review reviewWithID = null;
		try {
			review = crud.findByRestaurantIdAndUserId(restaurantId, userId);
		} catch (NoResultException e) {
			// ignore
		}

		if (review != null && id != null) {
			try {
				reviewWithID = crud.findById(id);
				if (reviewWithID != null && reviewWithID.getUser().getId().equals(userId) && reviewWithID.getRestaurant().getId().equals(restaurantId)) {
					review = null;
				}
			} catch (NoResultException e) {
				// ignore
			}
		}
		return review != null;
	}
}
