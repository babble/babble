
// test git

s = new Cloud.Site( "foo" );
s.giturl = "git://github.com/10gen/sample-student.git";

javaStatic( "ed.io.FileUtil" , "deleteDirectory" , javaCreate( "java.io.File" , "/data/tmp/externalgit/foo/" ) );

function test( fast ){

    var force = arguments[1];
    var sortFunc = arguments[2];

    var res = null;

    var time = Date.timeFunc( 
        function(){
            res = s.getGitBranchNames( force , sortFunc );
        }
    );

    if ( fast )
        assert( time < 50 );
    else
        assert( time > 50 );
    
    return res;
}

assert( test( false ).length > 1 );
assert( test( true ).length > 1 );
assert( test( false , true ).length > 1 );
