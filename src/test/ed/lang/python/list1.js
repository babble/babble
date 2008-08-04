local.src.test.ed.lang.python.listPython();

var pyL = getList();

assert( pyL.length == 2 );

assert( pyL.some( function(x){ return x == 1; } ) );
assert( pyL.every( function(x){ return x > 0; } ) );

var jsA = [1, 3, 5];

assert( pyManipList(jsA) );
