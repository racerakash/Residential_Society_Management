/*==========================================================
    Author      : Abhijit Kulkarni
    Date Created: 30 Dec 2016
    Description : Controller for view/Edit users info
    
    Change Log
    s.no      date    author     description     
    
 ===========================================================*/
(function () {
	
	'use strict';
	
	angular.module("SocietyManagementSystem").controller("MyInfoController",MyInfoController);
	
	
	MyInfoController.$inject=['UserService','StaticDataService', '$state', '$rootScope', 'FlashService','$log','$scope','ContextRoot','toaster'];
	
	function MyInfoController(UserService,StaticDataService, $state, $rootScope, FlashService,$log,$scope,ContextRoot,toaster){
		
		 var vm = this;

		 vm.emailFormat = /^[a-z]+[a-z0-9._]+@[a-z]+\.[a-z.]{2,5}$/;
		 vm.phoneNumber = /^(\+?(\d{1}|\d{2}|\d{3})[- ]?)?\d{3}[- ]?\d{3}[- ]?\d{4}$/;
	     vm.passWordFormat=/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])(?=.{8,})/;
	     
	     vm.flatsApiPath = ContextRoot + '/data/flats' +'.do';
	     vm.parkingApiPath = ContextRoot + '/data/parkings' +'.do';
		 
		 vm.saveUserInfo = saveUserInfo;
		 
	
		 (function initController() {
				// reset login status
				$log.debug("My Info controller initialised......");
				 		 
			 	UserService.getUserDetails($rootScope.user.userId,function(response){
				 
				 	if(response===401) {
	                    toaster.pop('error', "", "You are not authorised to perform this operation. Please contact system administrator.");
	                }else if(response===500) {
	                	toaster.pop('error', "", "Some thing went wrong at server side. Please contact system administrator.");
	                } else{
	                	vm.user=response;
	                } 
			 
		 	 	});

		})();
		 
	      function saveUserInfo() {
		 
		 	$log.debug("saveUserInfo mentod called...");
		 	
            vm.dataLoading = true;
            
            var userInfo= {
            	"userInfoId":vm.user.userInfoId,
    		    "primaryM1": vm.user.primaryM1,
    		    "alterEmailId":vm.user.alterEmailId,
    		    "emailId":vm.user.emailId,
    		    "isBoardMember":vm.user.isBoardMember,
    		    "isSecondaryOwner":vm.user.isSecondaryOwner,
    		    "secondaryM2":vm.user.secondaryM2,
    		    "primFirstName":vm.user.primFirstName,
    		    "primMiddleName":vm.user.primMiddleName,
    		    "primLastName":vm.user.primLastName,
    		    "secFirstName":vm.user.secFirstName,
    		    "secMiddleName":vm.user.secMiddleName,
    		    "secLastName":vm.user.secLastName,
    		    "correspondanceAddress":vm.user.correspondanceAddress,
    		    "userTypeId":1,
    		    "usersFlats":vm.user.usersFlats,
    		    "usersParkings":vm.user.usersParkings,
    		    "version":vm.user.version
	 		};
            
            UserService.updateUserInfo(userInfo,function(response) {
            	
            	debugger;
                if (response===200) {
                    toaster.pop('success', "", "Your personal information saved successfully");
                    $state.go('home.dashboard');
                } else if(response===401) {
                	toaster.pop('error', "","You are not authorised to perform this operation. Please contact system administrator.");
                    vm.dataLoading = false;
                }else if(response===500) {
                	toaster.pop('error', "","Some thing went wrong at server side. Please contact system administrator");
                    vm.dataLoading = false;
                }
                
            });
		 }
		
	}

})();