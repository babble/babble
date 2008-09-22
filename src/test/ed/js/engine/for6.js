
for ( var i = 0; ; i++ ) {
    print( i );
    if( i >= 3 ) {
        break;
    }
}

for (var i = 0; ; ){
    print( i++ );
    if( i >= 3 ) {
        break;
    }
}

var i = 0;
for ( ;; ){
    print( i++ );
    if( i >= 3 ) {
        break;
    }
}

i = 0;
for ( ; i < 3 ; ){
    print( i++ );
}

i = 0;
for ( ;; i++ ){
    print( i );
    if( i >= 3 ) {
        break;
    }
}
