
var a = 3;
var b = 4;

function foo( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();   
}
print( "4=" + foo( 1 , 2 ) );

function bar( a , b ){
    var v = function(){
        return a + 5 + b;
    }
    a = 100;
    return v();   
}
print( "108=" + bar( 2 , 3 ) );
