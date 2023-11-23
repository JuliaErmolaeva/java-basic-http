angular.module('app', []).controller('indexController', function ($scope, $http) {
    $scope.getItems = function () {
        $http.get('http://localhost:8189/items')
            .then(function (response) {
                $scope.items = response.data;
                console.log($scope.items);
            });
    };

    $scope.getItems();
});