
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

