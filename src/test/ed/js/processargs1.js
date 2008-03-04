f = function(){
    processArgs("a", "b", "c");
    return b;
};

assert(f(1, 2, 3) == 2);
