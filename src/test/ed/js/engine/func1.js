function foo( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();   
}

function bar(){
    return function(){
        print( "asd" );
    };
}

print( foo( 1 , 2 ) );

