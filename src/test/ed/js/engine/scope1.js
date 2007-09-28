
var a = 1;
var b = 2;

function foo( a , b ){
    return a + b ;
}

function bar( a , b ){
    var f = foo;
    return f( a , b );
}

function foobar( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();
}

function foobar2( a , b ){
    var v = function(){
        return a + b;
    }
    a = 50;
    b = 51;
    return v();
}

function foobar3( a , b ){
    var v = function(){
        return a + b;
    }
    return v;
}


var c = foo( a , b );
print( "3 = " + c );
print( "3 = " + bar( b , a ) );

print( "NaN = " + foo( 11 ) );

print( "13 = " + foobar( 5 , 7 ) );

print( "101 = " + foobar2( 5 , 7 ) );

a = 3; b = 4;
print( "7 = " + foobar3( a , b )() );
/*
var f = foobar3( a , b );
a = 4; b = 5;
print( "7 = " + f() );

*/
