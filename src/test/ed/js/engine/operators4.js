
var a = 5;
a += 1;
print( a );

b = Object();
b.a = 1;
b.a += 2;
print( b.a );

var c = 0;
function foo(){
    var d = c;
    c = c + 1;
    if ( d == 0 ){
        return "a";
    }
    if ( d == 1 )
        return "b";
    if ( d == 2 )
        return "c";
    if ( d == 3 )
        return "d";

    return "e";
}
b[ foo() ] += 4;
print( b.a );


a = 5;
f = ( a += 1 );
print( f );
print( a );

a = Object();
a.a = 5;
f = ( a.a += 1 );
print( f );
print( a.a );
