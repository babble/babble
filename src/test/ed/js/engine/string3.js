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
