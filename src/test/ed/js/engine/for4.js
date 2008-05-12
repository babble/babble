
print( "before" );
a = null;
for ( var i in a ){
    print( i );
}
print( "after" );


print( "before" );
a = { z : 1 };
for ( var i in a ){
    print( i );
}
print( "after" );
