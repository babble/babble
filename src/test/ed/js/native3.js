
a = [ 1 , 2 , 3 ];
assert.eq( "[1.0, 2.0, 3.0]" , javaStatic( "java.util.Arrays" , "toString" , a ) );

assert.eq( 2 , javaStatic( "ed.js.NativeHelp1" , "count" , [ javaCreate( "ed.js.NativeHelp1" ) , javaCreate( "ed.js.NativeHelp1" ) ] ) );

assert.eq( 5 , javaStatic( "ed.js.NativeHelp1" , "sum" , [ 2 , 3 ] ) );
