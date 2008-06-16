
assert( sysexec( "ls -la" ).out );
assert( sysexec( "ls -la" ).err != null );
assert( sysexec( "ls -la" ).out.match( /\.\./ ) );

assert( sysexec( "ls" ).out != sysexec( "ls" , null , null  , "src" ) );
