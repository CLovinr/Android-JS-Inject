# Android-JS-Inject
用于Android端的Js注入，从safe-java-js-webview-bridge改进而来，能够在java与js端间相互动态传递函数。

原始开源项目[Safe Java-JS WebView Bridge](https://github.com/pedant/safe-java-js-webview-bridge)


[使用例子](http://blog.csdn.net/wosisuisiwo/article/details/51524463)

##基本用法-Android

	 WebView webView = (WebView) findViewById(R.id.chat_webview);
	 WebSettings ws = wv.getSettings();
	 ws.setJavaScriptEnabled(true);//启用js
	 
	 Js js = new Js();//该类中的所有public、非静态的、第一个参数为WEBView的函数都表示是将要注入的方法。
	 webView.setWebChromeClient(new WEBChromeClient(false, false, true,
	                    new JsCallJava.InjectObj("chat.msg", js)));
	                    
上面的代码中，第一个false表示手动注入js，即要引入相关的js（这个在后面会讲到）来实现注入。暂时不推荐自动注入，因为在实际各种情况下应用时发现，会有极少数注入失败的情况（这个其实是无法容忍的），而手动注入还没碰见过注入失败的时候。
	                    
若Js类中含有无参构造函数(同时Js2,Js3等类也是)，则可以是这种形式:

	webView.setWebChromeClient(new WEBChromeClient(false, false, true,
		new JsCallJava.InjectObj("chat.page.ui", Js.class,Js2.class,Js3.class)));

##基本用法-JavaScript(手动注入)
1.在项目的[maven/Android-JS-Inject/src/main/resources目录](https://github.com/CLovinr/Android-JS-Inject/tree/master/maven/Android-JS-Inject/src/main/resources/safe-js)中有个我提供的用于注入的js文件inject.js,引入它到你的html页面中.

2.以jquery为例：

	$(document).ready(function() {
	    var isAutoInject=false;//目前推荐用false。
			window.injectReady(isAutoInject,function() {
						//注入完成的回调
			});
	});


  
