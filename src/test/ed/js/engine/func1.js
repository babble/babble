function foo( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();   
}

function bar(){
    return function(){
        return print( "asd" );
    };
}

function zoo(){
    function bar(){
        return 1;
    }
    return a;
}

print( foo( 1 , 2 ) );

