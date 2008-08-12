//
//  replication test : basic repl
//
//  Save an object in master
//  Wait for repl to slave
//  Save another object in master
//  Wait for repl to slave
//  check

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
    assert( cnt == 1 , "object count in slave != 1  found :  " + cnt);
     
    assert(dbm.test.checksum() == dbs.test.checksum());
    
    print( " **** adding second object to master ***" );
    
    dbm.test.save({n:2});
    assert.eq(dbm.test.count() , 2);

    print( " **** " + info.replTimeMS + "ms wait for replication***" );
    
    sleep(info.replTimeMS);
   
    assert( dbs.test.findOne() , "error : slave table empty" );
    
    // check to see that we have only 2 objects, and they are the right objects
    
    var n = dbs.test.count();
    assert( n == 2, "error count != 2 : " + n);
    
    n = dbs.test.find({n:1}).length();
    assert(n == 1, "incorrect count for {n:1} objects in slave table : " + n);

    n = dbs.test.find({n:2}).length();
    assert(n == 1, "incorrect count for {n:2} objects in slave table : " + n);
    
    assert(dbm.test.validate().valid, "master table test failed validation");
    assert(dbs.test.validate().valid, "slave table test failed validation");
});
