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
        .module('app.travel-review')
        .controller('TravelReviewFormController', TravelReviewFormController);

    TravelReviewFormController.$inject = ['$scope', 'TravelReview', 'User', 'messageBag', '$http'];

    function TravelReviewFormController($scope, TravelReview, User, messageBag, $http) {
        //Assign service to $scope variables
        $scope.reviewService = TravelReview;
        $scope.userService = User;
        $scope.messageService = messageBag;

        //Load users and external resources
        $scope.userService.data = User.query();

        $scope.taxis = [];
        $scope.hotels = [];
        $scope.flights = [];


        //** NOTE **
        //You should edit the parameter of the $http.get(...) function calls to specify the URL structure to return
        // Commodity entities from your TravelAgent API

        // EDIT ==>

        $http.get('api/travel/taxis')
            .success(function(data) {
                $scope.taxis = data;
            })
            .error(function() {
                $scope.messageService.push('danger', '/taxis resource unavailable');
            });
        $http.get('api/travel/hotels')
            .success(function(data) {
                $scope.hotels = data;
            })
            .error(function() {
                    $scope.messageService.push('danger', '/hotels resource unavailable');
            });
        $http.get('api/travel/flights')
            .success(function(data) {
                $scope.flights = data;
            })
            .error(function() {
                $scope.messageService.push('danger', '/flights resource unavailable');
            });

        // <== EDIT

        //Get today's date for the reviewDate form value min
        $scope.date = Date.now();

        $scope.review = {};
        // Define an addReview() function, which creates a new review via the REST service,
        // using those details provided and displaying any error messages
        $scope.addReview = function() {
            $scope.messageService.clear();

            $scope.reviewService.save($scope.review,
                //Successful query
                function(data) {

                    // Update the list of reviews
                    $scope.reviewService.data.push(data);

                    // Clear the form
                    $scope.reset();

                    //Add success message
                    $scope.messageService.push('success', 'Review made');
                    //Error
                }, function(result) {
                    for(var error in result.data){
                        $scope.messageService.push('danger', result.data[error]);
                    }
                }
            );

        };

    }
})();