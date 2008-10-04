
function foo(){
    o = { a : "b" };
    a = [ 1 , 2 ];
    
    for ( var i=0; i<a.length; i++ )
	print( i );
    
    for ( var i in o ){
	print( i );
    }
}

foo();
