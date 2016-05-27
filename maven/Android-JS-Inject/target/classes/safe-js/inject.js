(function(window, undefined) {

	var injectDebug = typeof window.injectDebug == "undefined" ? false : window.injectDebug;

	window.injectReady = window.injectReady || function(isAutoInject, callback) {
		var maxCount = 5;
		var index = 0;
		function check() {
			if (window["android_js_test"]) {
				if (injectDebug)
					console.log("<inject-ok>");
				alert("<inject-ok>");
				callback(index);
			} else if (index++ < maxCount) {
				if (isAutoInject == "false") {
					if (injectDebug)
						console.log("<inject-js>");
					alert("<inject-js>");
				}
				setTimeout(check, 20);
			} else {
				if (injectDebug)
					console.log("<inject-failed>")
				alert("<inject-failed>");
			}
		}
		check();
	};

	if (injectDebug)
		console.log("<inject-test>")
	alert("<inject-test>");

})(window);