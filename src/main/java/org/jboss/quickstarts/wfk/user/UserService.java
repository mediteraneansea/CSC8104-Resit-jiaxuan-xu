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
 * @see UserValidator
 * @see UserRepository
 */
//The @Dependent is the default scope is listed here so that you know what scope is being used.
@Dependent
public class UserService {

    @Inject
    private @Named("logger") Logger log;

    @Inject
    private UserValidator validator;

    @Inject
    private UserRepository crud;

    /**
     * <p>Create a new client which will be used for our outgoing REST client communication</p>
     */
    public UserService() {
    }

    /**
     * <p>Returns a List of all persisted {@link User} objects, sorted alphabetically by name.<p/>`
     *
     * @return List of User objects
     */
    List<User> findAll() {
        return crud.findAll();
    }

    /**
     * <p>Returns a single User object, specified by a Long id.<p/>
     *
     * @param id The id field of the User to be returned
     * @return The User with the specified id
     */
    public User findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Writes the provided User object to the application database.<p/>
     *
     * <p>Validates the data in the provided User object using a {@link UserValidator} object.<p/>
     *
     * @param user The User object to be written to the database using a {@link UserRepository} object
     * @return The User object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public User create(User user) throws ConstraintViolationException, ValidationException, Exception {
        log.info("UserService.create() - Creating " + user.getName());
        
        // Check to make sure the data fits with the parameters in the User model and passes validation.
        validator.validateUser(user);

        // Write the user to the database.
        return crud.create(user);
    }

    /**
     * <p>Deletes the provided User object from the application database if found there.<p/>
     *
     * @param user The User object to be removed from the application database
     * @return The User object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    User delete(User user) throws Exception {
        log.info("delete() - Deleting " + user.toString());

        User deletedUser = null;

        if (user.getId() != null) {
            deletedUser = crud.delete(user);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedUser;
    }
}
