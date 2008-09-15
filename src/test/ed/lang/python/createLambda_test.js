
func = javaStatic( "ed.lang.python.Python" , "extractLambda" , "def foo():\n    return 17\n" );
assert.eq( 17 , func() );
