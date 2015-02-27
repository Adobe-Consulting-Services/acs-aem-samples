/*global angular*/
var sampleModule = angular.module('angularjs-sample', ['ngRoute']);

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