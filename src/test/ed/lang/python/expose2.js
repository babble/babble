var someData = {x: 142, y: 'hi'};

getglobal = function(x){
    return someData[x];
};


local.src.test.ed.lang.python.module();

assert( pyX == someData.x );
assert( pyY == someData.y );
