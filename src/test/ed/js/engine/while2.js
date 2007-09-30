var foo = 5;
var bar = true;

while ( foo && bar ){
    print( foo );
    foo = foo + -1;
}

do {
    print( foo );
} while ( foo );

/*
t1:
while( bar ){
    print( "once" );
    break t1;
}

*/
