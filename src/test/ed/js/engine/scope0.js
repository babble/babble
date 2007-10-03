
var a = 1;
var b = 2;

function foo(){
    return a + b;
}

print( "3=" + foo() );
a = 2;
b = 3;
print( "5=" + foo() );

var v = 13;
function bar(){
    v = v + 2;
    return v;
}
print( bar() );
print( bar() );
print( v );

function asdads( a ){
    a = 5;
    var b = a + 2;
    return a + b;
}

    
