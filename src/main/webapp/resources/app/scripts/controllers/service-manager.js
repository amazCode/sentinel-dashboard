var app = angular.module('sentinelDashboardApp');

app.controller('ServiceManagerCtl', ['$scope', '$stateParams', 'MachineService', 'ngDialog',
  function ($scope, $stateParams, MachineService,ngDialog) {
    $scope.app = $stateParams.app;
    $scope.currentPage = 1;
    $scope.records = [];
    $scope.machinesPageConfig = {
      pageSize: 10,
      currentPageIndex: 1,
      totalPage: 1,
      totalCount: 0,
    };

    var serviceDetailDialog;
      $scope.addService = function () {
    	  $scope.serviceDetail = {
    			  serviceName: '',
    			  description: '',
    			  manufacturer: '',
    			  chargePerson: '',
    			  maintainPerson: '',
    		      };
    	  
          $scope.serviceDetailDialog = {
            title: '新增服务',
            type: 'add',
            confirmBtnText: '新增',
            showAdvanceButton: true,
          };
          serviceDetailDialog = ngDialog.open({
            template: '/app/views/dialog/service-detail-dialog.html',
            width: 680,
            overlay: true,
            scope: $scope
          });
        };
        
        $scope.editService = function (entity) {
            $scope.serviceDetail = angular.copy(entity);
            $scope.serviceDetailDialog = {
              title: '编辑服务',
              type: 'edit',
              confirmBtnText: '保存'
            };
            serviceDetailDialog = ngDialog.open({
              template: '/app/views/dialog/service-detail-dialog.html',
              width: 780,
              overlay: true,
              scope: $scope
            });
          };
        
        $scope.saveRule = function () {
    	    if ($scope.serviceDetailDialog.type === 'add') {
    	    	addRule($scope.serviceDetail);
    	      } else if ($scope.serviceDetailDialog.type === 'edit') {
    	        saveRule($scope.serviceDetail, true);
    	      }
        	
          }
        function saveRule(rule, edit) {
        	MachineService.saveRule(rule).success(function (data) {
              if (data.code == 0) {
            	  $scope.reloadServiceDetails();
                if (edit) {
                	serviceDetailDialog.close();
                }
              } else {
                alert('修改服务失败!' + data.msg);
              }
            });
          };
          
        function addRule(rule) {
        	MachineService.saveRule(rule).success(function (data) {
                if (data.code == 0) {
              	  $scope.reloadServiceDetails();
              	serviceDetailDialog.close();
                } else {
                  alert('失败：' + data.msg);
                }
              });
        }
        
        
        $scope.editServiceRecords = function (entity) {//服务详细
//        	 $("#save").hide();
        	 $scope.currentService =  angular.copy(entity);
        	 $scope.interfacePageConfig = {
        		  pageSize:10,
           	      currentPageIndex: 1,
           	      totalPage: 1,
           	      totalCount: 0,
           	    };
            $scope.serviceList ;
            $scope.serviceRecordsDialog = {
              title: '服务详细',
              type: 'edit',
              confirmBtnText: '保存'
            };
            serviceRecordsDialog = ngDialog.open({
              template: '/app/views/dialog/service-details-dialog.html',
              width: 1280,
              overlay: true,
              scope: $scope
            });
            MachineService.getServiceListByServiceRecord(entity.id,entity.serviceName).success(
                function (data) {
                  if (data.code == 0 && data.data) {
                    $scope.serviceList = data.data;
                    $scope.interfacePageConfig.totalCount = $scope.serviceList.length;
//                    $scope.interfacePageConfig.pageSize = $scope.serviceList.length;
                  }else {
                	   $scope.serviceList = [];
                  }
                }
              );
            
          };
          
          $scope.reStatisticsStatus = false;
          $scope.reStatisticsInterface = function() {
        	  MachineService.reStatisticsServiceInterface($scope.currentService, $scope.reStatisticsStatus).success(
                      function (data) {
                    	  if (data.code == 0 && data.data) {
                    		  $scope.serviceList = data.data;
                    		  $scope.interfacePageConfig.totalCount = $scope.serviceList.length;
                              $scope.interfacePageConfig.pageSize = $scope.serviceList.length;
                              alert('统计成功! 响应信息：' + data.msg);
                    	  }else{
                        	  alert('统计失败! 响应信息：  ' + data.msg);
                          }
                      }
                     );
          };
          
          $scope.editInterface = function(id,urlName,description) {
        	  if (id == null || id == undefined || id == '') {
             	 alert("操作出现异常，刷新页面重试，或者联系管理员");
                  return;
             }
        	  MachineService.saveInterface(id,urlName,description).success(
        			  function(data){
        				  if (data.code == 0) {
        					  alert('成功：' + data.msg);
        	                } else {
        	                  alert('失败：' + data.msg);
        	                }
        			  }
              )
//              alert('ce'+id);
          }
          
          $(document).on('click', '#save', function () {
//        	  $("#edit").show();
//       	   $("#save").hide();
              var urlName=$(this).parents("tr").find("td").eq(1).text(); //接口名
              var description=$(this).parents("tr").find("td").eq(3).text();//描述
              var id = $(this).parents("tr").find("td").eq(8).text();//id
//              alert(urlName+'   '+description+'  '+id);
              $scope.editInterface(id,urlName,description);
              
         })
         
          $(document).on('click', '#edit', function (id) {
        	  alert('接口名和接口描述点击可更改')
//        	  $("#save").show();
//        	  $("#edit").hide();
//              
         })
          
        	   
        $scope.myFrame = function (data) {
//            $scope.serviceDetail = angular.copy(data);
            $scope.urlAddressDialog = {
            	value:data
//              title: '编辑服务',
//              type: 'edit',
//              confirmBtnText: '保存'
            };
            urlAddressDialog = ngDialog.open({
              template: '/app/views/dialog/url-address-dialog.html',
              width: 500,
              overlay: true,
              scope: $scope
            });
          };
         
         
          
        $scope.onOpenAdvanceClick = function () {
            $scope.serviceDetailDialog.showAdvanceButton = false;
          };
          $scope.onCloseAdvanceClick = function () {
            $scope.serviceDetailDialog.showAdvanceButton = true;
          };
          
    
    
    $scope.reloadServiceDetails = function() {
      MachineService.getServiceDetails().success(
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
    
   
      
     
    
    $scope.removeServiceRecord = function(id) {
        if (id == null || id == undefined || id == '') {
        	 alert("移除该服务出现异常，请联系管理员");
             return;
        }
        MachineService.removeServiceRecord(id).success(
          function(data) {
            if (data.code == 0) {
              $scope.reloadServiceDetails();
            } else {
              alert("remove failed");
            }
          }
        );
      };
    
    $scope.reloadServiceDetails();
    
  }]);
