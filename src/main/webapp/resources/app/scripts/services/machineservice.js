var app = angular.module('sentinelDashboardApp');

app.service('MachineService', ['$http', '$httpParamSerializerJQLike',
  function ($http, $httpParamSerializerJQLike) {
    this.getAppMachines = function (app) {
      return $http({
        url: 'app/' + app + '/machines.json',
        method: 'GET'
      });
    };
    this.getRequestRecords = function (app) {
        return $http({
          url: 'app/' + app +'/requestrecord.json',
          method: 'GET'
        });
      };
      this.getServiceListByServiceRecord = function (id,name) {
          return $http({
            url: 'app/list/service/'+id+'/'+name,
            method: 'GET'
          })
        };
      this.getServiceDetails = function () {
          return $http({
            url: 'app/servicedetail.json',
            method: 'GET'
          });
        };
        this.getRequestDetail = function () {
            return $http({
              url: 'app/servicedetail.json',
              method: 'GET'
            });
          };
        
        this.getRequestDetail = function (entity,type) {
        	var param = {
        			resource:entity.resource,
        			type:type
        	}
            return $http({
              url: 'app/request/detail',
              params: param,
              method: 'GET'
            });
          };
        
      this.saveRule = function (rule) {
          var param = {
        		  id:rule.id,
        		  serviceName: rule.serviceName,
        		  description: rule.description,
        		  manufacturer: rule.manufacturer,
        		  chargePerson: rule.chargePerson,
        		  maintainPerson: rule.maintainPerson,
             };
          return $http({
              url: 'app/service/detail/save.json',
              params: param,
              method: 'PUT'
          });
      };
      
      this.reStatisticsServiceInterface = function (currentService,reStatisticsStatus) {
    	  var param = {
    			  id:currentService.id,
    			  name:currentService.serviceName,
    			  status:reStatisticsStatus
    	  }
    	  return $http({
              url: 'app/service/interface/restatistics',
              params: param,
              method: 'GET'
          });
      }
      
      this.saveInterface = function (id,urlName,description) {
    	  var param = {
    			  id:id,
    			  urlName:serviceName,
    			  description:description
    	  }
    	  return $http({
              url: 'app/service/interface/save',
              params: param,
              method: 'PUT'
          });
      }
      
      
      
      this.removeServiceRecord = function (id) {
          return $http({
            url: 'app/servicedetail/remove/'+id,
            method: 'DELETE',
          });
        };
      
    this.removeAppMachine = function (app, ip, port) {
      return $http({
        url: 'app/' + app + '/machine/remove.json',
        method: 'POST',
        headers: {
          'Content-type': 'application/x-www-form-urlencoded; charset=UTF-8'
        },
        data: $httpParamSerializerJQLike({
          ip: ip,
          port: port
        })
      });
    };
  }]
);
