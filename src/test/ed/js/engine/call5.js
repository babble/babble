

function foo(){
    return this.a;
}

print( foo.call( { a : 5 } ) );


function foo2( z ){
    return this.a + z;
}

print( foo2.call( { a : 5 } , 3 ) );


