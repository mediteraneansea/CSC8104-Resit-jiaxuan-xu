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
(function () {
    'use strict';

    angular
        .module('app.user')
        .controller('UserListController', UserListController);

    UserListController.$inject = ['$scope', '$filter', 'User', 'messageBag'];

    /**
     * Description of File
     * @author hugofirth
     * @constructor
     */
    function UserListController($scope, $filter, User, messageBag) {
        //Assign service to $scope variables
        $scope.userService = User;
        $scope.messageService = messageBag;

        //Divide contact list into several sub lists according to the first character of their name property
        var getHeadings = function(users) {
            var headings = {};
            for(var i = 0; i<users.length; i++) {
                //Get the first letter of a user's firstName
                var startsWithLetter = users[i].name.charAt(0).toUpperCase();
                //If we have encountered that first letter before then add the user to that list, else create it
                if(headings.hasOwnProperty(startsWithLetter)) {
                    headings[startsWithLetter].push(users[i]);
                } else {
                    headings[startsWithLetter] = [users[i]];
                }
            }
            return headings;
        };

        //Get users
        $scope.userService.query(
            //Successful query
            function(data) {
                $scope.userService.data = data;
                $scope.users = getHeadings($scope.userService.data);
                //Keep the contacts list headings in sync with the underlying users
                $scope.$watchCollection('userService.data', function(newUsers, oldUsers) {
                    $scope.users = getHeadings(newUsers);
                });
            },
            //Error
            function(result) {
                for(var error in result.data){
                    $scope.messages.push('danger', result.data[error]);
                }
            }
        );

        //Boolean flag representing whether the details of the contacts are expanded inline
        $scope.details = false;

        //Default search string
        $scope.search = "";

        //Continuously filter the content of the users list according to the contents of $scope.search
        $scope.$watch('search', function(newValue, oldValue) {
            $scope.users = getHeadings($filter('filter')($scope.userService.data, $scope.search));
        });
    }

})();
