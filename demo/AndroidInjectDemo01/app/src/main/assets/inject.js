(function(window, undefined) {

	var injectDebug = typeof window.injectDebug == "undefined" ? false : window.injectDebug;

	var injectName = "android_js_test";

	if (window[injectName]) {
		sendCmd("<inject-ok>");
		return;
	}

	window._sendCmd_=function(cmd){
		sendCmd(cmd);
	};

	function sendCmd(cmd) {
		if (injectDebug) {
			console.log(cmd);
		}
		alert(cmd);
		if(cmd=="<inject-ok>"){
			if(typeof window["autoInjectReady"]=="function"){
				try{window["autoInjectReady"]();}catch(e){}
				window["autoInjectReady"]=null;
			}
			window.autoInjectOk=true;

		}
	}

	window.injectReady = window.injectReady || function(isAutoInject, callback) {
		var maxCount = 10;
		var index = 0;

		function check() {
			if (window[injectName]) {
				sendCmd("<inject-ok>");
				callback(index);
			} else if (index++ < maxCount) {
				if (isAutoInject == "false"||!isAutoInject) {
					sendCmd("<inject-js>");
				}
				setTimeout(check, 20);
			} else {
				sendCmd("<inject-failed>");
			}
		}
		check();
	};

	sendCmd("<inject-test>");

})(window);