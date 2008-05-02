
t = fork( function( foo ){
        return foo + 1;
    } , 5 );
t.start();
assert( 6 == t.returnData() );
