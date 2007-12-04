
try {
    print(5);
}
finally {
    print( "f" );
}

try {
    print(5);
}
catch( foo ){
    print( foo );
    asd = 1;
}
finally {
    print( "f" );
}


try {
    print(5);
}
catch( foo ){
    print( foo );
}

print( "----" );

try {
    print(5);
    if ( 5 == 5 )
        throw(7);
    print(1);
}
catch( foo ){
    print( foo );
    asd = 1;
}
finally {
    print( "f" );
}


