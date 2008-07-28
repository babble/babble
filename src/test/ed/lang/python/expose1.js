var called = {};

getglobal = function(x){
    called[x] = true;
};


local.src.test.ed.lang.python.module();

assert( called.x );
assert( called.y );
