function foo(a, b, c){
    return arguments.length;
};

print( foo( 1 , 2 , 3 , null ) );

print( foo( null ) );
print( foo( null , 5 ) );
print( foo( null , 5 , null ) );

