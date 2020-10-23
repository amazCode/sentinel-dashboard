var app = angular.module('sentinelDashboardApp');

app.controller('RequestRecordCtl', ['$scope', '$stateParams', 'MachineService', 'ngDialog',
  function ($scope, $stateParams, MachineService,ngDialog) {
    $scope.app = $stateParams.app;//'服务网关' ;
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
    
    
    $scope.requestDetail = function (entity,type) {//请求详细
    	$scope.requestDetailList ;
    	 $scope.requestDetailPageConfig = {
          	      currentPageIndex: 1,
//          	      pageSize:10,
//          	      totalPage: 1,
          	      totalCount: 0,
          	    };
    	
        $scope.requestDetailDialog = {
          title: '请求详细',
          confirmBtnText: '保存'
        };
        requestDetailDialog = ngDialog.open({
          template: '/app/views/dialog/request-detail-dialog.html',
          width: 980,
          overlay: true,
          scope: $scope
        });
        
        MachineService.getRequestDetail(entity,type).success(
                function (data) {
                  if (data.code == 0 && data.data) {
                    $scope.requestDetailList = data.data;
                    $scope.requestDetailPageConfig.totalCount = $scope.requestDetailList.length;
//                    $scope.requestDetailPageConfig.pageSize = $scope.requestDetailList.length;
                  }
                }
              );
    }
    
    
    
    $scope.exceptionRequest = function() {
    	alert( 'test');
    }
    
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
