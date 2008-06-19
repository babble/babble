
function good( num ){
    var x = 6;
    for ( var i=0; i<num ; i++ ){
        x += 2;
    }
    //print( i.getClass() );
    return x;
}

function bad( num ){
    var x = 6;
    var i = "abc"
    for ( var i=0; i<num; i++ ){
        x += 2;
    }
    //print( i.getClass() );
    return x;
}

var num = 20000;
var numCalls = 10;

assert( good( num ) == bad( num ) );

print( "inf1" );
for ( var i=0; i<5; i++ ){

    var a = Date.timeFunc( good , numCalls , num );
    var b = Date.timeFunc( bad , numCalls , num );

    b += Date.timeFunc( bad , numCalls , num );
    a += Date.timeFunc( good , numCalls , num );

    if ( i == 0 )
        continue; // for jit

    print( "\t good: " + a  + " bad: " + b );
    assert( a * 2 < b , "too slow");
}


function silly(){
    var i = 5;
    var j = 6;

    if ( j )
        print("ok");

    if ( ! j )
        print("ok");

    if ( ! ( i + j ) )
        print("ok");

    if ( i != j )
        print( "ok" );

}
