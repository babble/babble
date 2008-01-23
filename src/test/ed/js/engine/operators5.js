
print( 5 || 6 );
print( 5 || 6 );
if ( false || 5 )
    print( "hi " );
print( "5" || "6" );

var a = "asd";
var b = "123";

print( a || b );
print( b || a );

a = null;

print( a || b );
print( b || a );

print( a || b || "123" );

print( 25 * "5" );
print( 5 * "a" );

print( 25 / "10" );
print( 5 / "a" );

print( 5 + "12" );
print( 5 + "a" );
print( "12" + 5 );


print( 5 - "12" );
print( 5 - "a" );

for ( var i=0; i<=25; i++ ){
    print( (i/5).toFixed() );
    print( (i/5).toFixed(1) );
    print( (i/5).toFixed(2) );
    print( (i/5).toFixed(3) );
}
