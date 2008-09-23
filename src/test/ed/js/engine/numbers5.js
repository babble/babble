
print( 11111111111111111111111111111111111 > 123132 );
print( Number( "11111111111111111111111111111111111" ) > 21312 );

var s = "1";
for ( var i=0; i<100; i++){
    s += "1";
    print( Number( s ) > 1 );
    print( Number( "-" + s ) > 1 );
    print( Number( "-" + s + ".1" ) > 1 );
}
