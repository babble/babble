

s = new Cloud.Site( "laksjdalsdj" );

assert.eq( 0 , s.dbs.length );
assert( s.upsertDB( "dev" , "test1" ) );
assert( ! s.upsertDB( "dev" , "test1" ) );
assert.eq( 1 , s.dbs.length );

assert.raises( 
    function(){
        assert( s.upsertDB( "devasdasd" , "asdasds" ) );        
    }
);
assert.eq( 1 , s.dbs.length );
assert.eq( "dev" , s.findDB( "dev" ).name );
assert.eq( "test1" , s.findDB( "dev" ).server );
assert.eq( "test1" , s.findDB( s.dbs[0].id ).server );


assert.eq( 0 , s.environments.length );
assert( s.upsertEnvironment( "dev" , "master" , null , "test1" ) );
assert.eq( 1 , s.environments.length );
assert.eq( "dev" , s.environments[0].db );
assert( ! s.upsertEnvironment( "dev" , "master" , null , "test1" ) );


assert.raises(
    function(){
        assert( s.upsertEnvironment( "dev" , "master" , "a123123xssd" , "test1" ) );
    }
);

assert.raises(
    function(){
        assert( s.upsertEnvironment( "dev" , "master" , null , "teasdasd" ) );
    }
);
