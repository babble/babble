var arr = new Array();

var before = arr.approxSize();

arr.push("moooooooooooooooo");

var size0 = arr.approxSize();
assert.gt(size0, before, "Array size didn't change");

// consistant?
arr.push( "foo" );
var size1 = arr.approxSize();
arr.pop();
var size2 = arr.approxSize();

assert( Math.abs( size0 - size2 ) < 200 );
assert( size0 < size1 );

var rar = [1,2,3,4];
size3 = rar.approxSize();

arr = arr.concat( rar );
assert( size0 < arr.approxSize() );
assert( size3 + size0 > arr.approxSize() );

// try adding a bunch of things
var prevSize = rar.approxSize();
for( var i=0; i<100; i++ ) {
    rar.unshift( i );
    var temp = rar.approxSize();
    assert( temp > prevSize );
    prevSize = temp;
}

// arrays are bigger than simple objs
assert( [].approxSize() > {}.approxSize() );
