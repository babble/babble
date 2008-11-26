/*
var tmp = openFile('/tmp');
for each (var file in tmp.listFiles()){
    if(file.getName().startsWith('jxp-'))
       var jxpDirectory = file.getName();
}

var recurse = function(){
    sysexec('./runAnt.bash ed.js.Shell src/test/ed/js/eval1.js -exit');
    var translatedFile = openFile('/tmp/'+jxpDirectory+'/ed/js/gen/src_test_ed_js_eval1_js1.java');

    return translatedFile.lastModified().getTime();
}

var old = recurse();
var foo = recurse();
assert.eq(old, foo, "needlessly recompiled");

sysexec('touch src/main/ed/js/JSInternalFunctions.java');
var foo = recurse();
assert.lt(old, foo, "didn't recompile");

*/
