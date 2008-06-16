
assert( sysexec( "/bin/ls -la" ).out );
assert( sysexec( "/bin/ls -la" ).err != null );
assert( sysexec( "/bin/ls -la" ).out.match( /\.\./ ) );

assert( sysexec( "/bin/ls" ).out != sysexec( "ls" , null , null  , "src" ) );
