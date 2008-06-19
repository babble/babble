
assert( sysexec( "/bin/ls -la" ).out );
assert( sysexec( "/bin/ls -la" ).err != null );
assert( sysexec( "/bin/ls -la" ).out.match( /\.\./ ) );

assert( sysexec( "/bin/ls" ).out != sysexec( "/bin/ls" , null , null  , "src" ) );
