
a = [ 1 , 2 , 3 ];
assert.eq( "[1, 2, 3]" , javaStatic( "java.util.Arrays" , "toString" , a ).toString().replace( /\.\d+/g , "" ) );

assert.eq( 2 , javaStatic( "ed.js.NativeHelp1" , "count" , [ javaCreate( "ed.js.NativeHelp1" ) , javaCreate( "ed.js.NativeHelp1" ) ] ) );

assert.eq( 5 , javaStatic( "ed.js.NativeHelp1" , "sum" , [ 2 , 3 ] ) );

assert.eq( 2 , javaStatic( "ed.js.NativeHelp1" , "count2" , 5 , 2 ) );
assert.eq( 3 , javaStatic( "ed.js.NativeHelp1" , "count2" , 5 , 2 , 6 ) );


assert( 5 , javaStatic( "ed.js.NativeHelp1" , "varArgWhich" , 0 , 5 , 6 , 7 ) );
assert( 6 , javaStatic( "ed.js.NativeHelp1" , "varArgWhich" , 1 , 5 , 6 , 7 ) );

assert( 5 , javaStatic( "ed.js.NativeHelp1" , "varArgWhich" , 0 , 5 ) );
