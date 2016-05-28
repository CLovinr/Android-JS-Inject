

(function(global){
    var log=<LOG>;
    if(log)
        console.log("<NAMESPACE_COMMON> initialization begin");
    var _index=0;
    var searchMore=<SEARCH_MORE>;/*是否递归搜索。若为否，则{f:function(){},obj:{f2:function(){}}}中只会转换函数f而f2不会被转换。*/
    function _ID(){
        return ""+_index++;
    }


    <COMMON_NAMESPACES>;
    var callJava = null;

    function _parseFun(fun,callJavaObj){
        var id =_ID();
        callJavaObj.queue[id] = fun;
        return "<JSON_FUNCTION_STARTS>"+id;
    }

    function _parseArrayFun(arr,callJavaObj){
        for(var i=0;i<arr.length;i++){
        	arr[i]=_parseObjFun(arr[i],callJavaObj);
        }
        return arr;
    }

    /*在传递对象到java层之前，转换里面的函数*/
    function _parseObjFun(obj,callJavaObj){

		if(typeof obj=="function"){
			return _parseFun(obj,callJavaObj);
		}
		else if(obj instanceof Array){
			return _parseArrayFun(obj,callJavaObj);
		}
		else{
			var robj={};
	        for(var name in obj){
	            var arg=obj[name];
	            var type = typeof arg;
	            if(type==="function"){
	                robj[name]=_parseFun(arg,callJavaObj);
	            }else if(searchMore&&type==="object"&&arg!==null){
	                if(arg instanceof Array){
	                    robj[name]=_parseArrayFun(arg,callJavaObj);
	                }else{
	                    robj[name]=_parseObjFun(arg,callJavaObj);
	                }

	            }else{
					robj[name]=arg;
				}
	        }
	        return robj;
        }

    };
	function _addJavaCallback(callbackId,isPermanent,callJavaObj){
			var callFun = function(){
				var rs=	callJava.apply(callJavaObj.namespace,[callJavaObj,callJavaObj.namespaceStr,true,<JAVA_CALLBACK>,callbackId,"callback"].concat(Array.prototype.slice.call(arguments, 0)));
				return rs;
			};
			callFun.destroy=function(){
				callJava.apply(callJavaObj.namespace,[callJavaObj,callJavaObj.namespaceStr,true,<JAVA_CALLBACK>,callbackId,"destroy"]);
			};
			callFun.setPermanent=function(isPermanent){
				callJava.apply(callJavaObj.namespace,[callJavaObj,callJavaObj.namespaceStr,true,<JAVA_CALLBACK>,callbackId,"setPermanent",isPermanent?true:false]);
			};
			return callFun;
	 };
	var javaCallbackTag = <JAVA_CALLBACK>;
	/*转换json对象中的指定格式的字符串（与某个java对象Java2JsCallback对应的）为js函数*/
	function parseString2Fun(obj,isPermanent,callJavaObj){
	    var type = typeof obj;
	    var returnObj=obj;
		if(type==="string"&&obj.indexOf(javaCallbackTag)==0){/*java端的回调,从java端传递函数过来。*/
			var index = javaCallbackTag.length;
			var callbackId = obj.substr(index);
			returnObj = _addJavaCallback(callbackId,isPermanent,callJavaObj);
	     }else if(searchMore&&type==="object"&&obj!=null){
	        if(obj instanceof Array){
	            for(var i=0;i<obj.length;i++){
	                returnObj[i]=parseString2Fun(obj[i],isPermanent,callJavaObj);
	            }
	        }else{
	            for(var x in obj){
            	     returnObj[x]=parseString2Fun(obj[x],isPermanent,callJavaObj);
            	}
	        }

	     }
	     return returnObj;
	};


     callJava =  function (){
            var args = Array.prototype.slice.call(arguments, 0);
            var callJavaObj = args.shift();
            var namespaceStr=args.shift();
            var isJavaCallback=args.shift();

            if (args.length < 1) {
                throw namespaceStr+" call error, message:miss method name";
            }
            var aTypes = [];
            for (var i = 1;i < args.length;i++) {
                var arg = args[i];
                var type = typeof arg;
                aTypes[aTypes.length] = type;
                if (type === "function"||(type==="object"&&arg!==null)) {
                    args[i] = _parseObjFun(arg,callJavaObj);
                }
            }
            var res = JSON.parse(prompt(JSON.stringify({
                isJavaCallback:isJavaCallback,
                method: args.shift(),
                types: aTypes,
                args: args,
                namespace:namespaceStr
            })));
            if (res.code != 200) {
                throw namespaceStr+" call error, code:" + res.code + ", message:" + res.result;
            }
            return res.result;
    };



    <NAMESPACE_COMMON> = function(namespace,namespaceStr){
    	
	    var returnObj =	 {
	    	namespace:namespace,
	    	namespaceStr:namespaceStr,
	        queue: {},
	        destroy:function(id){/*用于清除注册的函数*/
	            if(log){
	                console.log("before delete function(id="+id+"):\n"+returnObj.queue[id]);
	                delete returnObj.queue[id];
	                console.log("after delete \""+id+"\":"+returnObj.queue[id]);
	            }else{
	                delete returnObj.queue[id];
	            }
	        },
	        callback: function () {/*对应于java端的apply函数*/
	            var args = Array.prototype.slice.call(arguments, 0);
	            var id = args.shift();
	            var isPermanent = args.shift();
	
	            for(var i=0;i<args.length;i++){
	                args[i] = parseString2Fun(args[i],isPermanent,returnObj);
	            }
	            returnObj.queue[id].apply(returnObj, args);
	            if (!isPermanent) {
	                returnObj.destroy(id);
	            }
	        },
	        commonFunciton:function(){
	        	
			    namespace.callback=returnObj.callback;
			    namespace.destroy=returnObj.destroy;
			    var commonFun= function () {
			        return callJava.apply(namespace,[returnObj,namespaceStr,false].concat(Array.prototype.slice.call(arguments, 0)));
			    };
			   
			    return commonFun;
	        },initOk:function(){
	        	/*有时候，我们希望在该方法执行前插入一些其他的行为用来检查当前状态或是监测
			    代码行为，这就要用到拦截（Interception）或者叫注入（Injection）技术了*/
			    /**
			     * Object.getOwnPropertyName 返回一个数组，内容是指定对象的所有属性
			     *
			     * 其后遍历这个数组，分别做以下处理：
			     * 1. 备份原始属性；
			     * 2. 检查属性是否为 function（即方法）；
			     * 3. 若是重新定义该方法，做你需要做的事情，之后 apply 原来的方法体。
			     */
			    Object.getOwnPropertyNames(namespace).forEach(function (property) {
			        var original = namespace[property];
			        if (typeof original === "function"&&property!=="callback"&&property!=="destroy") {
			            namespace[property] = function () {
			                return original.apply(namespace,  [property].concat(Array.prototype.slice.call(arguments, 0)));
			            };
			        }
			        
			    });
	        }
	    };
	    
	    return returnObj;
    
    } 
    
    
    global.<NAMESPACE_COMMON> = <NAMESPACE_COMMON>;
    if(log)
        console.log("<NAMESPACE_COMMON> initialization end");
})(window);
