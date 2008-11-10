
function go( o , path ){
    return javaStatic( "ed.js.JS" , "path" , o , path );
}

x = { a : 1 };
assert.eq( 1 , go( x , "a" ) );
assert( ! go( x , "a.b" ) );
assert( ! go( x , "b" ) );

x = { a : 1 ,
      b : { c : 2 }
    } ;
assert.eq( 2 , go( x , "b.c" ) );

x = { a : { b : { c : 3 } } }
assert.eq( 3 , go( x , "a.b.c" ) );
