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
(function() {
    'use strict';
    angular
        .module('app.user')
        .controller('UserFormController', UserFormController);

    UserFormController.$inject = ['$scope', '$routeParams', 'User', 'messageBag'];

    function UserFormController($scope, $routeParams, User, messageBag) {
        //Assign service to $scope variables
        $scope.userService = User;
        $scope.messageService = messageBag;
        $scope.user = {};
        $scope.create = true;
        //If $routeParams has :contactId then load the specified contact, and display edit controls on contactForm
        if($routeParams.hasOwnProperty('userId')) {
            $scope.user = $scope.userService.current;
            $scope.create = false;
        }

        // Define a reset function, that clears the prototype new User object, and
        // consequently, the form
        $scope.reset = function() {
            // Sets the form to it's pristine state
            if($scope.userForm) {
                $scope.userForm.$setPristine();
            }

            // Clear input fields. If $scope.user was set to an empty object {},
            // then invalid form values would not be reset.
            // By specifying all properties, input fields with invalid values are also reset.
            $scope.user = {name: "", phoneNumber: "", email: ""};

            // clear messages
            $scope.messageService.clear();
        };

        // Define an addUser() function, which creates a new user via the REST service,
        // using those details provided and displaying any error messages
        $scope.addUser = function() {
            $scope.messageService.clear();

            $scope.userService.save($scope.user,
                //Successful query
                function(data) {

                    // Update the list of users
                    $scope.userService.data.push(data);

                    // Clear the form
                    $scope.reset();

                    //Add success message
                    $scope.messageService.push('success', 'User added');
                    //Error
                }, function(result) {
                    for(var error in result.data){
                        $scope.messages.push('danger', result.data[error]);
                    }
                }
            );

        };

        // Define a saveUser() function, which saves the current user using the REST service
        // and displays any error messages
        $scope.saveUser = function() {
            $scope.messageService.clear();
            $scope.user.$update(
                //Successful query
                function(data) {
                    //Find the user locally by id and update it
                    var idx = _.findIndex($scope.userService.data, {'id': $scope.user.id});
                    $scope.userService.data[idx] = data;
                    //Add success message
                    $scope.messageService.push('success', 'User saved');
                    //Error
                }, function(result) {
                    for(var error in result.data){
                        $scope.messages.push('danger', result.data[error]);
                    }
                }
            )
        };
    }
})();