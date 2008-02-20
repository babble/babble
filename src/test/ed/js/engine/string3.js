var s = "asdasd";

print( s.charCodeAt( 1 ) );
print( s.charAt( 1 ) );
print( s.substring( 1 ) );
print( s.substring( 1 , 2 ) );
print( s.indexOf( "s" ) );
print( s.indexOf( "s" , 3 ) );
print( s.lastIndexOf( "s" ) );
print( s.lastIndexOf( "s" , s.length ) );

for ( var i=0; i<s.length; i++ )
    print( s.lastIndexOf( "s" , s.length - i ) );


print( "a=b".split( "=" ).length );
print( "a=b".split( "=" )[0] );
print( "a=b".split( "=" )[1] );

print( "a=".split( "=" ).length );
print( "a=".split( "=" )[0] );
print( "a=".split( "=" )[1] );

