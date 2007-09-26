function foo( a , b ){
    var v = function(){
        return a + 1 + b;
    }
    return v();   
}

print( foo( 1 , 2 ) );

