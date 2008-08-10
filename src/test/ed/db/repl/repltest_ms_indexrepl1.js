//
//  replication test : index repl
//
//  do nothing.
//  check to see that zero objects and zero indexes exist in m and s
//  create index in master
//  check repl
//  

__path__._repl( 
    function( dbm , dbs, info){

    // ensure no objects or indices in collection on master or slave

    assert(dbm.test.count() == 0);
    assert(dbs.test.count() == 0);
    assert(dbm.test.getIndexes().length() == 0);
    assert(dbs.test.getIndexes().length() == 0);

    // now create index on master and validate
    
    dbm.test.ensureIndex({n:1});
    assert(dbm.test.getIndexes().length() == 1);
    dbm.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    
    print( " **** " + info.replTimeMS + "ms wait for replication***" );
    
    sleep(info.replTimeMS);
    
    // check to see if both master and slave have index
   
    assert(dbm.test.getIndexes().length() == 1);
    assert(dbs.test.getIndexes().length() == 1);
    dbm.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    dbs.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    
    // now nuke the index, sleep, and validate that the drop propogated

    dbm.test.dropIndexes();
   
    assert(dbm.test.getIndexes().length() == 0);
   
    print( " **** " + info.replTimeMS + "ms wait for replication***" );
    
    sleep(info.replTimeMS);

    assert(dbm.test.getIndexes().length() == 0);
    assert(dbs.test.getIndexes().length() == 0);
    
    assert(dbm.test.validate().valid, "master table test failed validation");
    assert(dbs.test.validate().valid, "slave table test failed validation");
});
