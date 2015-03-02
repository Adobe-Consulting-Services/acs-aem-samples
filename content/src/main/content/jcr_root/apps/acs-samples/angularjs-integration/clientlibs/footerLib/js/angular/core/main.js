/*global angular*/

/*
 * This is the definition of the application module itself as well as core configurations like routing and the http service.
 * Since this file defines that module that all of our controllers and services are part of, it needs to be listed first in the clientlibs js.txt file.
 */
var sampleModule = angular.module('angularjs-sample', ['ngRoute']);

/*
 * The routeProvider configuration maps URL paths to AJAX content to be loaded into the ng-view container defined in the containerPage component.
 * Note that the templateUrls are each using a .content Sling selector.  This allows us to switch out the content portion of the containerPage with that of the partials.
 */
sampleModule.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
                when('/home', {
                    templateUrl: '/content/acs-samples/angularjs-integration/home.content.html',
                    controller: 'homePartialController'
                }).
                when('/search', {
                    templateUrl: '/content/acs-samples/angularjs-integration/search.content.html',
                    controller: 'searchPartialController'
                }).
                when('/checkout', {
                    templateUrl: '/content/acs-samples/angularjs-integration/checkout.content.html',
                    controller: 'checkoutPartialController'
                }).
                otherwise({
                    redirectTo: '/home'
                });
    }]);