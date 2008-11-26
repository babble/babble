var tmp = openFile('/tmp');
for each (var file in tmp.listFiles()){
    if(file.getName().startsWith('jxp-'))
       var jxpDirectory = file.getName();
}

var edPath = openFile('.').getAbsolutePath();
var genFile = openFile("/tmp/" + jxpDirectory + "/ed/js/gen/src_test_ed_js_eval1_js1.java");
var internJavaFile = openFile('src/main/ed/js/JSInternalFunctions.java');

var recurse = function(){
    sysexec(edPath + '/runAnt.bash ed.js.Shell src/test/ed/js/eval1.js --exit');
    return genFile.lastModified();
}

var errMsg = function(str) {
    var genPath = (genFile)? genFile.getAbsolutePath() : "null";
    var internJavaPath = (internJavaFile)?internJavaFile.getAbsolutePath() : "null";
    return str + "[ edPath= " + edPath + ", genFile=" + genPath + ", internJavaFile: " + internJavaPath + "]";
}

var old = recurse();
var foo = recurse();
assert.eq(old, foo, errMsg("needlessly recompiled"));


internJavaFile.touch();
var foo = recurse();
assert.lt(old, foo, errMsg("didn't recompile"));
