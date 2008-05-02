
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


print( "----" );

var a = 4;
var b = 5;
a += 5, b = a;
print(a);
print(b);

var a = 4;
var b = 5;
print( ( b = a + b , a = b ) );
print(a);
print(b);

print( "---" );



a = { y : 1 };
print( "y" in a );
print( "z" in a );

a = 5;
print( a );
print( -a );

a = null;
print( a );
a || ( a = 1 );
print( a );

print( ! "hi" in { hi : 5 } );
print( ! ( "hi" in { hi : 5 } ) );
print( "hi" in { hi : 5 } );
print( ! ( "hi" in { hiz : 5 } ) );


print( ! "hi" in { "false" : 5 } );


A = function(){
    this.x = 1;
};
A.prototype.y = 2;
a = new A();
a.z = 3;

with( a ){
    print( x );
    print( y );
    print( z );
}
