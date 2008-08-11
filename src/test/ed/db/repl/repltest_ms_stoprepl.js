//
//  replication test : see if replication stops
//
//  Save an object in master
//  Wait for repl to slave
//  Check to see that it made it to slave
//  Stop replication
//  Save another object in master
//  Ensure that it didn't make it to slave

__path__._repl( 
    function( dbm , dbs, info){

    dbm.test.save({n:1});

    assert( dbm.test.findOne() );
    assert( dbm.test.findOne().n == 1 );
    assert.eq( dbm.test.count() , 1 );

    print( " **** " + info.replTimeMS + "ms wait for replication***" );
    
    sleep(info.replTimeMS);
    
    assert( dbs.test.findOne() , "nothing in slave table" );
    assert( dbs.test.findOne().n == 1 , "what's in slave table is wrong" );

    var cnt = dbs.test.count();
    assert( cnt == 1 , "objet count in slave != 1  found :  " + cnt);
    
    
    //  now, stop the replication
    
    print( " **** removing source to stop replication ***" );
    
    connect( "127.0.0.1:" + info.slavePort + "/local" ).sources.drop();

    print( " **** " + info.replTimeMS + "ms wait for slave stopage***" );
    
    sleep(info.replTimeMS);
 
     print( " **** adding second object to master ***" );
    
    dbm.test.save({n:2});
    assert.eq(dbm.test.count() , 2);

    print( " **** " + info.replTimeMS + "ms wait for replication***" );
    
    sleep(info.replTimeMS);
    
    // nothing should be changed
    
    assert( dbs.test.findOne() , "nothing in slave table" );
    assert( dbs.test.findOne().n == 1 , "what's in slave table is wrong" );
    
    var cnt = dbs.test.count();
    assert( cnt == 1 , "objet count in slave != 1  found :  " + cnt);

    assert(dbm.test.validate().valid, "master table test failed validation");
    assert(dbs.test.validate().valid, "slave table test failed validation");
});
