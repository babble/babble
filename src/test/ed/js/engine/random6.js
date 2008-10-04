
function foo( a ){
    with( a ){
	return foo;
    }
}

print( foo( { foo : "asd" } ) );
