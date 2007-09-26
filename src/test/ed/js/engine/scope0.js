
var a = 1;
var b = 2;

function foo(){
    return a + b;
}

SYSOUT( "3=" + foo() );
a = 2;
b = 3;
SYSOUT( "5=" + foo() );

var v = 13;
function bar(){
    v = v + 2;
    return v;
}
SYSOUT( bar() );
SYSOUT( bar() );
SYSOUT( v );

function asdads( a ){
    a = 5;
    var b = a + 2;
    return a + b;
}

    
