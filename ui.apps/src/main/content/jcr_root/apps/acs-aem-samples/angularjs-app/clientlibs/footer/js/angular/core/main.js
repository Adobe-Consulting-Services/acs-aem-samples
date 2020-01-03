/*global angular*/

/*
 * This is the definition of the application module itself as well as core configurations like routing and the http service.
 * Since this file defines that module that all of our controllers and services are part of, it needs to be listed first in the clientlibs js.txt file.
 */
var sampleModule = angular.module('angularjs-sample', ['ngRoute']);

/*
 * The routeProvider configuration maps URL paths to AJAX content to be loaded into the ng-view container defined in the container-page
 * component.  Note that the templateUrls are each using a .content Sling selector.  Since our base-page component uses a content.jsp that is
 * overridden by the page partial component, this allows us to load only the content.jsp portion into the ng-view content area.
 * 
 * When using the app, note that the URL in the browser will be for the container page (angularjs-integration.html), followed by a hash 
 * matching the patterns below.  A second request will be sent matching the template URL.  A side-benefit of the way that this works is that
 * things like URL params will not be sent to the dispatcher when loading these page partials, allowing for effective caching of them on the
 * dispatcher while still allowing us to parse and use the URL parameters in the client-side Javascript.
 */
sampleModule.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
                when('/home', {
                    templateUrl: '/content/acs-samples/angularjs-app/home.content.html',
                    controller: 'homePartialController'
                }).
                when('/search', {
                    templateUrl: '/content/acs-samples/angularjs-app/search.content.html',
                    controller: 'searchPartialController'
                }).
                when('/checkout', {
                    templateUrl: '/content/acs-samples/angularjs-app/checkout.content.html',
                    controller: 'checkoutPartialController'
                }).
                otherwise({
                    redirectTo: '/home'
                });
    }]);