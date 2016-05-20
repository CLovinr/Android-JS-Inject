(function(window, undefined) {

	/**
	 * 查询地址参数。
	 *<pre>
	 * 若url=http://localhost/User/login.html?id=123#floor=1
	 * queryParam("id",false)->123
	 * queryParam("floor",true)->1
	 * queryParam("id",true)->null
	 *</pre>
	 * @param {Object} name 要查询的名称
	 * @param {Object} afterSharp 是否在#号后面。
	 */
	window.queryParam = function(name, afterSharp) {

		var findStr = afterSharp ? window.location.hash : window.location.search;

		var reg = new RegExp((afterSharp ? "(#|&)" : "(\\?|&)") + name + "=([^&]*)", "g");
		var r;
		if (afterSharp) {
			r = findStr.match(reg);
		} else {
			r = findStr.match(reg);
		}
		if (r != null) {
			var str = r.pop();
			var index = str.indexOf("=");
			r = str.substr(index + 1);
		}
		if (r != null) {
			r = decodeURI(r);
		}
		return r;
	};

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

	/**
	 * 用于js绑定。
	 * @param {Object} selector 可为null，用于搜索的元素选择器。
	 * @param {Object} prefixClass 绑定的接口类的全名。
	 */
	window.jsBinderBridge = window.jsBinderBridge || function(selector, prefixClass) {

		var domObjs = $(selector ? selector : document.body).find("[id]");
		var ids = [];
		domObjs.each(function() {
			var id = $(this).attr("id");
			if (id) {
				ids.push({
					"id": id
				});
			}
		});

		var data = {
			"ids": ids,
			"prefixClass": prefixClass,
			"setCall": function(id, name, value) {

				var obj = $("#" + id);
				switch (name) {
					case "onclick":
						obj.on("click", function() {
							value();
						});
						break;

					case "ATTR_ENABLE":
						obj.attr("disable", !value);
						break;
					case "ATTR_FOCUS_REQUEST":
						obj.focus();
						break;
					case "ATTR_VISIBLE":
						obj.css("display", value ? "" : "none");
						break;
					case "ATTR_VALUE":
						obj.val(value);
						break;
					case "ATTR_VALUE_CHANGE_LISTENER":
						obj.on("change", function() {
							value({
								"value":$(this).val()
							});
						});
						break
				}
			},
			"getCall": function(cid, nameIds) {

				var returnObj = {
					"values": null
				};

				var as = [];
				returnObj.values = as;

				for (var i = 0; i < nameIds.length; i++) {
					var id = nameIds[i].id;
					var name = nameIds[i].name;
					var val = null;
					var obj = $("#" + id);
					switch (name) {

						case "ATTR_ENABLE":
							val = !obj.attr("disable");
							break;

						case "ATTR_VISIBLE":
							val = obj.css("display") != "none";
							break;
						case "ATTR_VALUE":
							val = obj.val();
							break;
						case "ATTR_BOUNDS":
							{
								var offset = obj.offset();
								val = [offset.left, offset.top, obj.width(), obj.height()];
							}
							break
					}
					as.push(val);

				}

				window["android_js_test"].jsBinderGetter(cid, returnObj);
			}
		};

		window["android_js_test"].jsBinder(data);

		window.onbeforeunload = function() {
			window["android_js_test"].jsBinderRelease();
		};
	};

})(window);