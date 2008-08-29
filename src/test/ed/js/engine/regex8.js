
print( /s/.test( "s" ) );
print( /_/.test( "_" ) );

print( /[s_]/.test( "s" ) );
print( /[s_]/.test( "_" ) );

print( /^\/(.+)$/.test( "/asd" ) );
print( /^\/([.]+)$/.test( "/asd" ) );
print( /^\/([.]+)$/.test( "/as_da" ) );
print( /^.([.]+)$/.test( "/as_da" ) );
print( /^.([._]+)$/.test( "/wilson_hammer" ) );
