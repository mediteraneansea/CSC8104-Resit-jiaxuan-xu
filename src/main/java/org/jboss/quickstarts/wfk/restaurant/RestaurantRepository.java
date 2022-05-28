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

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

/**
 * <p>This is a Repository class and restaurants the Service/Control layer (see {@link RestaurantService} with the
 * Domain/Entity Object (see {@link Restaurant}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 *
 * @author Jiaxuan Xu
 * @see Restaurant
 * @see EntityManager
 */
public class RestaurantRepository {

	@Inject
	private @Named("logger")
	Logger log;

	@Inject
	private EntityManager em;

	/**
	 * <p>Returns a List of all persisted {@link Restaurant} objects, sorted alphabetically by phonenumber.</p>
	 *
	 * @return List of Restaurant objects
	 */
	List<Restaurant> findAll() {
		TypedQuery<Restaurant> query = em.createNamedQuery(Restaurant.FIND_ALL, Restaurant.class);
		return query.getResultList();
	}

	/**
	 * <p>Returns a single Restaurant object, specified by a Long id.<p/>
	 *
	 * @param id The id field of the Restaurant to be returned
	 * @return The Restaurant with the specified id
	 */
	Restaurant findById(Long id) {
		return em.find(Restaurant.class, id);
	}

	/**
	 * <p>Returns a single Restaurant object, specified by a String phonenumber.</p>
	 *
	 * <p>If there is more than one Restaurant with the specified phonenumber, only the first encountered will be returned.<p/>
	 *
	 * @param phonenumber The phonenumber field of the Restaurant to be returned
	 * @return The first Restaurant with the specified phonenumber
	 */
	Restaurant findByPhonenumber(String phonenumber) {
		TypedQuery<Restaurant> query = em.createNamedQuery(Restaurant.FIND_BY_PHONENUMBER, Restaurant.class).setParameter("phonenumber", phonenumber);
		return query.getSingleResult();
	}

	/**
	 * <p>Persists the provided Restaurant object to the application database using the EntityManager.</p>
	 *
	 * <p>{@link EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
	 * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
	 *
	 * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
	 *
	 * @param restaurant The Restaurant object to be persisted
	 * @return The Restaurant object that has been persisted
	 * @throws ConstraintViolationException, ValidationException, Exception
	 */
	Restaurant create(Restaurant restaurant) throws ConstraintViolationException, ValidationException, Exception {
		log.info("RestaurantRepository.create() - Creating " + restaurant.getName());

		// Write the restaurant to the database.
		em.persist(restaurant);

		return restaurant;
	}

}
