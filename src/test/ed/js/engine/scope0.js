
var a = 1;
var b = 2;

function foo(){
    return a + b;
}

SYSOUT( "3=" + foo() );
a = 2;
b = 3;
SYSOUT( "5=" + foo() );

