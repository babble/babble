
a = { z : 5 };
with( a ) print( z );

b = { z : 6 };
with( a ){
    print( z );
    with( b ){
        print( z );
    }
    print( z );
}

