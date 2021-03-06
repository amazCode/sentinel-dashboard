var app = angular.module('sentinelDashboardApp');

app.service('ServiceManagerService', ['$http', '$httpParamSerializerJQLike',
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
