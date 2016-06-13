
/**
 * 用于注册接口类。
 */
(function(global){
    var log=<LOG>;
    if(log)
        console.log("<HOST_APP> initialization begin");

    <HOST_APP_NAMESPACES>;
	global.<HOST_APP> = <HOST_APP>;
	
	var handleObj=<NAMESPACE_COMMON>(<HOST_APP>,<NAMESPACE>);
	
    <HOST_APP_FUN> handleObj.commonFunciton();
    handleObj.initOk();
   
    if(log)
        console.log("<HOST_APP> initialization end");
})(window);
