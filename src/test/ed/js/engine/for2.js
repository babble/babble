var b = 17;
print( b );
for ( var i = 5 ; i >= 0 ; i = i + -1 ){
    print( i );
    var b = i;
}
print( b );


var o = Object();
o.a = 5;
o.b = 6;
for ( bar in o ){
    print( bar );
}

function silly( ooo ){
    for ( var i in ooo ){
        print( i + " : " + ooo[i] );
    }
}

silly( o );
