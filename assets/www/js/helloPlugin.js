
window.helloPlugin = function() {
  cordova.exec(successFunction, failFunction, "UpdatePlugin","updatePlugin",[]);
}


var updatePlugin = {
    updateEvent: function(str){
   		cordova.exec(successFunction, failFunction, "UpdatePlugin","updatePlugin",[]); //无参就写[]，多个参数的顺序一定不能搞错  
	 }
 }
  
function successFunction(){  
  //如果在callbackContext.success中传递的是json对象，这里也可以直接读取。  
	 alert("OK");
}  
  
function failFunction(){  
 	alert("ERROR");
    //与successFunction同理，取决于callbackContext.error中传递的信息。  
}  

var init = function() {
	document.addEventListener("deviceready", onDeviceReady, true);
}

//var onDeviceReady = function() {
//	console.log("deviceready event fired");
////执行插件
//	window.helloPlugin("HELLO DATE!!!" , function(echoValue) {
//		console.log("rrrrrrrrrrrrrrrrrrrrrrrrr");
//	});
//};