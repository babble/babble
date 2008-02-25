

function foo(){
    return this.a;
}

print( foo.call( { a : 5 } ) );

function foo2( z ){
    return this.a + z;
}

print( foo2.call( { a : 5 } , 3 ) );

function bar(a, b, c){
    print(a);
    print(b);
    print(c);
    return(this.a);
}

print( bar.call( { a: 5}, 1, 2, 3));

print( bar.apply( {a: 5}, [1, 2, 3]));


function bar2(a, b, c){
    print(a == null);
    print(b == null);
    print(c == null);
}

bar2.apply( {a: 5}, [1]);

