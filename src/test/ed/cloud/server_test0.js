
function testServerParse( host , location , provider , number ){
    var s = new Cloud.Server( host )
    
    if ( ! location ){
        assert( ! s.real , "not supposed to be real but it is" );
        return;
    }

    assert( s.real , "not real" );
    assert.eq( location , s.location );
    assert.eq( provider , s.provider );
    assert.eq( number , s.number );
}

testServerParse( "iad-sb-n5.10gen.cc" , "iad" , "sb" , 5 );
testServerParse( "iad-sb-n7.10gen.cc" , "iad" , "sb" , 7 );
testServerParse( "iad-sb-n7" , "iad" , "sb" , 7 );
testServerParse( "abc-def-n99.10gen.cc" , "abc" , "def" , 99 );

testServerParse( "adasdsad" );
testServerParse( "adasdsad-abc-n1" );

testServerParse( "ip-10-251-170-224" , "use1c" , "ec2" , "10251170224" );
testServerParse( "ip-10-251-170-224" , "use1c" , "ec2" , 10251170224 );
