
r = javaStatic( "ed.net.httpserver.HttpRequest" , "getDummy" , "/abc/123?z=y" );
assert.eq( "y" , r.z );
assert.eq( 1 , r.getParameterNames().length );
assert.eq( "z" , r.getParameterNames()[0] );
assert.eq( "", r.getServletPath() );
assert.eq( "/abc/123", r.getPathInfo() );
