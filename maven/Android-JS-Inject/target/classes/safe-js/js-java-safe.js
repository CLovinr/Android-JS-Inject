

(function(global){
    var log=<LOG>;
    if(log)
        console.log("<HOST_APP> initialization begin");

    <HOST_APP_NAMESPACES>;
    	

	global.<HOST_APP> = <HOST_APP>;
	
	var obj=<NAMESPACE_COMMON>(<HOST_APP>,<NAMESPACE>);
	
    <HOST_APP_FUN> obj.commonFunciton();
    obj.initOk();
   
    
    if(log)
        console.log("<HOST_APP> initialization end");
})(window);
