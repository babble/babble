var e = Exception.Quiet("hi");

assert(e instanceof Exception.Quiet);

try{
    throw e;
}
catch(e){
    assert(e instanceof Exception.Quiet);
}