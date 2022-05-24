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
    //Define routes for top-level 'app' module
    angular
        .module('app')
        .config(config);

    config.$inject = ['$httpProvider', '$routeProvider'];

    function config($httpProvider, $routeProvider) {
        //Use a HTTP interceptor to add a nonce to every GET request to prevent MSIE from caching responses.
        $httpProvider.interceptors.push('ajaxNonceInterceptor');

        //Note that this app is a single page app, composed of multiple 'views'
        //Each 'view' is some combination of a template and a controller
        //A 'view' is routed to using a URL fragment following a # symbol. For example, to select the 'home' route, the
        // URL is http://localhost:8080/jboss-contacts-angularjs/#/home
        $routeProvider.
            //If URL fragment is '/home', load the Travel Agent review form template, with the associated Controller
            when('/home', {
                templateUrl: 'templates/travel-review/reviewForm.html',
                controller: 'TravelReviewFormController'
                //If URL fragment is '/users/add', then load the userForm.html template, with UserController
            }).when('/users', {
                templateUrl: 'templates/user/userList.html',
                controller: 'UserListController'
            }).when('/users/add', {
                templateUrl: 'templates/user/userForm.html',
                controller: 'UserFormController'
            }).when('/users/:userId', {
                templateUrl: 'templates/user/viewUser.html',
                controller: 'UserController'
            }).when('/about', {
                templateUrl: 'templates/about.html'
                // Add a default route
            }).otherwise({
                redirectTo: '/home'
            });
    }
})();