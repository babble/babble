
var r = /the/g;
print( r );
print( "abe and the cow".match( r ) );
print( "abe and th cow".match( r ) );
print( "abe and the cow".match( "the.[abcow]\+" ) );

print( "foo the the the cow".replace( /the/ , "___" ) );
print( "foo the the the cow".replace( /the/g , "___" ) );

print( "the 123 food".match( /\d+/ ) );

