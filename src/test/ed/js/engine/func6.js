
a = function b() {
    print( "hi" );
}

a();


a = function b( num ) {
    if( num > 0 ) {
        print( num-- );
        b( num );
    }
}

a(3);

// b should be null, which causes a ref 
// error for proper js, but not us
try {
    b
    if( b == null )
        print( "ok" );
}
catch(e) {
    print( "ok" );
}


var foo = { a : [function bar( num ) {
    if( num > 0 ) {
        print( num-- );
        bar( num );
    }
}] };

foo.a[0]( 3 );

