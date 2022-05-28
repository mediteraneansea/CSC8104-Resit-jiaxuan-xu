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
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * <p>This is a Repository class and reviews the Service/Control layer (see {@link ReviewService} with the
 * Domain/Entity Object (see {@link Review}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 *
 * @author Jiaxuan Xu
 * @see Review
 * @see EntityManager
 */
public class ReviewRepository {

	@Inject
	private @Named("logger")
	Logger log;

	@Inject
	private EntityManager em;

	/**
	 * <p>Returns a List of all persisted {@link Review} objects, sorted alphabetically by phonenumber.</p>
	 *
	 * @return List of Review objects
	 */
	List<Review> findAll() {
		TypedQuery<Review> query = em.createNamedQuery(Review.FIND_ALL, Review.class);
		return query.getResultList();
	}

	/**
	 * <p>Returns a single Review object, specified by a Long id.<p/>
	 *
	 * @param id The id field of the Review to be returned
	 * @return The Review with the specified id
	 */
	Review findById(Long id) {
		return em.find(Review.class, id);
	}

	/**
	 * <p>Returns a single Review object, specified by restaurantId and reviewDate.</p>
	 *
	 * <p>If there is more than one Review with the specified restaurantId and reviewDate, only the first encountered will be returned.<p/>
	 *
	 * @param restaurantId      The restaurantId field of the Reviews
	 * @param userId The user id of the Reviews
	 * @return The first Review with the specified restaurantId and reviewDate
	 */
	Review findByRestaurantIdAndUserId(Long restaurantId, Long userId) {
		TypedQuery<Review> query = em.createNamedQuery(Review.FIND_BY_RESTAURANT_ID_AND_USER_ID, Review.class)
				.setParameter("restaurantId", restaurantId)
				.setParameter("userId", userId);
		return query.getSingleResult();
	}

	/**
	 * <p>Returns a list of Review objects, specified by userId.<p/>
	 *
	 * @param userId The userId field of the Reviews to be returned
	 * @return The Reviews with the specified seats
	 */
	List<Review> findAllByUserId(Long userId) {
		TypedQuery<Review> query = em.createNamedQuery(Review.FIND_ALL_BY_USER_ID, Review.class)
				.setParameter("userId", userId);
		return query.getResultList();
	}


	/**
	 * <p>Persists the provided Review object to the application database using the EntityManager.</p>
	 *
	 * <p>{@link EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
	 * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
	 *
	 * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
	 *
	 * @param review The Review object to be persisted
	 * @return The Review object that has been persisted
	 * @throws ConstraintViolationException, ValidationException, Exception
	 */
	Review create(Review review) throws ConstraintViolationException, ValidationException, Exception {
		log.info("ReviewRepository.create() - Creating " + review.getUser().getId() + " " + review.getRestaurant().getId());

		// Write the review to the database.
		em.persist(review);

		return review;
	}

	/**
	 * <p>Deletes the provided Review object from the application database if found there</p>
	 *
	 * @param review The Review object to be removed from the application database
	 * @return The Review object that has been successfully removed from the application database; or null
	 * @throws Exception
	 */
	Review delete(Review review) throws Exception {
		log.info("ReviewRepository.delete() - Deleting " + review.getUser().getId() + " " + review.getRestaurant().getId());

		if (review.getId() != null) {
			/*
			 * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(),
			 * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent
			 * context, the object isn't known as in a persistent state in this new context, so you have to merge it.
			 *
			 * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database
			 * to reattach it.
			 *
			 * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument.
			 * You first need an object in a persistent state to be able to delete it.
			 *
			 * Therefore we merge first and then we can remove it.
			 */
			em.remove(em.merge(review));

		} else {
			log.info("ReviewRepository.delete() - No ID was found so can't Delete.");
		}

		return review;
	}

}
