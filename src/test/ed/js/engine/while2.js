var foo = 5;
var bar = true;

while ( foo && bar ){
    print( foo );
    foo = foo + -1;
}

do {
    print( foo );
} while ( foo );

t1:
while( bar ){
    print( "once 1" );
    break t1;
}

while( bar ){
    print( "once 2" );
    break;
}

foo = 2;
while ( foo >= 0 ){
    foo = foo + -1;
    if ( foo != 0 )
        continue;
    print( "once 3" );
}
