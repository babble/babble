
print( 11111111111111111111111111111111111 > 123132 );
print( Number( "11111111111111111111111111111111111" ) > 21312 );

var s = "1";
for ( var i=0; i<30; i++){
    s += "1";
    print( Number( s ) > 1 );
    print( Number( "-" + s ) > 1 );
    print( Number( "-" + s + ".1" ) > 1 );
}

print( "---" );

print( Number( "1E1" ) == 10 );

print( Number('3.156E5') );
