
Struct = {}

Struct.__rnew = function(){
    var a = arguments;
    var start = 0;

    var def = null;
    if ( arguments.length > 0 && String.isUpper( arguments[0][0] ) ){
        start = 1;
        def = arguments[0];
    }

    f = function(){
        var pos = 0;
        for ( var i=start; i<a.length; i++ ){
            this[a[i]] = arguments[pos++];
        }
    }

    if ( def )
        scope.getThreadLocal()[ def ] = f;
    
    return f;
}
