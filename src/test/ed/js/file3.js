
assert( "foo.jpg" == javaStatic( "ed.js.JSFile" , "cleanFilename" , "foo.jpg" ) );
assert( "foo.jpg" == javaStatic( "ed.js.JSFile" , "cleanFilename" , "asd/asda/sd/a/foo.jpg" ) );
assert( "foo.jpg" == javaStatic( "ed.js.JSFile" , "cleanFilename" , "z:\\asd\\asd\\foo.jpg" ) );
