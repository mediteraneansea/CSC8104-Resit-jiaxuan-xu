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
        .controller('UserController', UserController);

    UserController.$inject = ['$scope', '$routeParams', 'User', 'TravelReview', 'messageBag'];

    function UserController($scope, $routeParams, User, TravelReview, messageBag) {
        //Assign service to $scope variables
        $scope.userService = User;
        $scope.messageService = messageBag;
        $scope.reviewService = TravelReview;
        $scope.reviews = [];


        //Get routeParam and user data
        var id = $routeParams.userId;
        $scope.userService.get(
            {userId: id},
            //Successful query
            function(data) {
                $scope.userService.current = data;
            },
            //Error
            function(result) {
                for(var error in result.data){
                    $scope.messages.push('danger', result.data[error]);
                }
            }
        );

        //Get reviews for user
        $scope.reviewService.query(
            {user: $scope.userService.current.id},
            //Successful query
            function(data) {
                $scope.reviews = data;
            },
            //Error
            function(result) {
                for(var error in result.data){
                    $scope.messages.push('danger', result.data[error]);
                }
            }
        );

        $scope.removeReview = function(review) {
            console.log("Delete attempted");
            $scope.reviewService.delete(
                {reviewId: review.id},
                //Successful query
                function(data) {
                    var idx = _.findIndex($scope.reviews, {'id': review.id});
                    if(idx>-1){
                        $scope.reviews.splice(idx, 1);
                    }
                },
                //Error
                function(result) {
                    for(var error in result.data){
                        $scope.messages.push('danger', result.data[error]);
                    }
                }
            )
        }
    }
})();