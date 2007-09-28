
var a = 3;
var b = 4;

function foo( a , b ){
    var v = function(){
        return a + b;
    }
    return v;
}

function foo2( a , b ){
    var v = function(){
        print( c );
        return a + b;
    }
    a = 100;
    return v;
}



var f = foo( a , b );
var f2 = foo2( a , b );

a = 4; b = 5;

var c = 2;

print( "7 = " + f() );
print( "104 = " + f2() );
