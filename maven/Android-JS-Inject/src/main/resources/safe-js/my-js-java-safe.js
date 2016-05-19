

(function(global){
    var log=<LOG>;
    if(log)
        console.log("<HOST_APP> initialization begin");
    var _index=0;
    var searchMore=<SEARCH_MORE>;/*是否递归搜索。若为否，则{f:function(){},obj:{f2:function(){}}}中只会转换函数f而f2不会被转换。*/
    function ID(){
        return ""+_index++;
    }
    
    function parseObjFun(obj){
		var robj={};
        for(var name in obj){
            var arg=obj[name];
            var type = typeof arg;
            if(type==="function"){
                var id =ID();
                <HOST_APP>.queue[id] = arg;
                robj[name]="<JSON_FUNCTION_STARTS>"+id;
            }else if(searchMore&&type==="object"&&arg!==null&&!(arg instanceof Array)){
                robj[name]=parseObjFun(arg);
            }else{
				robj[name]=arg;
			}
        }
        
        return robj;
    }

   var callJava =  function (){
                var args = Array.prototype.slice.call(arguments, 0);
                var isJavaCallback=args.shift();

                if (args.length < 1) {
                    throw "<HOST_APP> call error, message:miss method name";
                }
                var aTypes = [];
                for (var i = 1;i < args.length;i++) {
                    var arg = args[i];
                    var type = typeof arg;
                    aTypes[aTypes.length] = type;
                    if (type === "function") {
                        var id =ID();
                        <HOST_APP>.queue[id] = arg;
                        args[i] = id;
                    }else if(type==="object"&&arg!==null){
                        args[i] = parseObjFun(arg);
                    }
                }
                var res = JSON.parse(prompt(JSON.stringify({
                    isJavaCallback:isJavaCallback,
                    method: args.shift(),
                    types: aTypes,
                    args: args,
                    namespace:namespace
                })));
                if (res.code != 200) {
                    throw "<HOST_APP> call error, code:" + res.code + ", message:" + res.result;
                }
                return res.result;
        };


    <HOST_APP_NAMESPACES>;
    var namespace=<NAMESPACE>;
    <HOST_APP> = {
        queue: {},
        destroy:function(id){/*用于清除注册的函数*/
            if(log){
                console.log("before delete function(id="+id+"):\n"+this.queue[id]);
                delete this.queue[id];
                console.log("after delete \""+id+"\":"+this.queue[id]);
            }else{
                delete this.queue[id];
            }
        },
        callback: function () {/*对应于java端的apply函数*/
            var args = Array.prototype.slice.call(arguments, 0);
            var id = args.shift();
            var isPermanent = args.shift();

            function addJavaCallback(callbackId){
				var callFun = function(){
					var rs=	callJava.apply(<HOST_APP>,[true,<JAVA_CALLBACK>,callbackId,"callback"].concat(Array.prototype.slice.call(arguments, 0)));
					return rs;
				};
				callFun.destroy=function(){
					callJava.apply(<HOST_APP>,[true,<JAVA_CALLBACK>,callbackId,"destroy"]);
				};

				callFun.setPermanent=function(isPermanent){
					callJava.apply(<HOST_APP>,[true,<JAVA_CALLBACK>,callbackId,"setPermanent",isPermanent?true:false]);
				};

				return callFun;
            };

            for(var i=0;i<args.length;i++){
                var value = args[i];
                if(typeof value==="string"&&value.indexOf(<JAVA_CALLBACK>)==0){/*java端的回调,从java端传递函数过来。*/
					var index = <JAVA_CALLBACK>.length;
					var callbackId = value.substr(index);
					args[i]=addJavaCallback(callbackId);
                }
            }
            this.queue[id].apply(this, args);
            if (!isPermanent) {
                this.destroy(id);
            }
        }
    };



    <HOST_APP_FUN> function () {
        return callJava.apply(<HOST_APP>,[false].concat(Array.prototype.slice.call(arguments, 0)));
    };
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
    Object.getOwnPropertyNames(<HOST_APP>).forEach(function (property) {
        var original = <HOST_APP>[property];
        if (typeof original === "function"&&property!=="callback"&&property!=="destroy") {
            <HOST_APP>[property] = function () {
                return original.apply(<HOST_APP>,  [property].concat(Array.prototype.slice.call(arguments, 0)));
            };
        }
    });
    global.<HOST_APP> = <HOST_APP>;
    if(log)
        console.log("<HOST_APP> initialization end");
})(window);
