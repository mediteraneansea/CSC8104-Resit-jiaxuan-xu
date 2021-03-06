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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check User objects against arbitrary requirements.</p>
 *
 * @author Jiaxuan Xu
 * @see User
 * @see UserRepository
 * @see Validator
 */
public class UserValidator {
    @Inject
    private Validator validator;

    @Inject
    private UserRepository crud;

    /**
     * <p>Validates the given User object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing user with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param user The User object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If user with the same email already exists
     */
    void validateUser(User user) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(user.getEmail(), user.getId())) {
            throw new UniqueEmailException("Unique Email Violation");
        }
    }

    /**
     * <p>Checks if a user with the same email address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the User class.</p>
     *
     * <p>Since Update will being using an email that is already in the database we need to make sure that it is the email
     * from the record being updated.</p>
     *
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
     */
    boolean emailAlreadyExists(String email, Long id) {
        User user = null;
        User userWithID = null;
        try {
            user = crud.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }

        if (user != null && id != null) {
            try {
                userWithID = crud.findById(id);
                if (userWithID != null && userWithID.getEmail().equals(email)) {
                    user = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return user != null;
    }
}
