var app = angular.module('sentinelDashboardApp');

app.controller('RequestRecordCtl', ['$scope', '$stateParams', 'MachineService',
  function ($scope, $stateParams, MachineService) {
    $scope.app = $stateParams.app;
    $scope.propertyName = '';
    $scope.reverse = false;
    $scope.currentPage = 1;
    $scope.records = [];
    $scope.machinesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    $scope.sortBy = function (propertyName) {
      // console.log('machine sortBy ' + propertyName);
      $scope.reverse = ($scope.propertyName === propertyName) ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };
    
    $scope.reloadMachines = function() {
      MachineService.getRequestRecords($scope.app).success(
        function (data) {
          // console.log('get machines: ' + data.data[0].hostname)
          if (data.code == 0 && data.data) {
            $scope.records = data.data;
            $scope.machinesPageConfig.totalCount = $scope.records.length;
          } else {
            $scope.records = [];
          }
        }
      );
    };
    $scope.reloadMachines();
    
  }]);
