
try {
    throw 5;
}
catch ( e if e == 5  ){
    print( e );
}



try {
    throw 5;
}
catch ( e if e == 6 ){
    print( "a" );
}
catch ( e if e == 5 ){
    print( "b" );
}



try {
    throw 5;
}
catch ( e if e == 5 ){
    print( "c" );
}
catch ( e if e > 2 ){
    print( "d" );
}


try {
    throw 5;
}
catch ( e if e == 6 ){
    print( "e" );
}
catch ( e ){
    print( "f" );
}


try {
    try {
        throw 5;
    }
    catch ( e if e == 6 ){
        print( "blah1" );
    }
}
catch ( foo ){
    print( "blah2" );
}







