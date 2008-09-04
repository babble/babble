// _repl.js
/* 
framework for replication testing
takes 1 paramter, the function to test.
that function takes 2 paramters, first is master db, second is slave db
*/

dbDir = "../p/db/";
masterPort = 5001;
slavePort = 5002;

try {
    // start master
    sysexec( "rm -rf /tmp/db-master" );
    sysexec( "mkdir /tmp/db-master/" );
    sysexec( "touch /tmp/db-master/a" );
    
    master = fork( 
	function(){
	    print( "MASTER starting" );
	    sysexec( dbDir + "db --master --port " + masterPort + " --dbpath /tmp/db-master/" , null , null , null ,
		     { out : function( s ){ print( "MASTER out: " + s ) } ,
		       err : function( s ){ print( "MASTER err: " + s ) }
		 }
		   );
	}
    );
    
    master.start();
    
    
    
    // start slave
    sysexec( "rm -rf /tmp/db-slave" );
    sysexec( "mkdir /tmp/db-slave/" );
    sysexec( "touch /tmp/db-slave/a" );
    slave = fork(
	function(){
	    print( "SLAVE starting" );
	    sysexec( dbDir + "db --slave --port " + slavePort + " --dbpath /tmp/db-slave/" , null , null , null ,
		     { out : function( s ){ print( "SLAVE out: " + s ) } ,
		       err : function( s ){ print( "SLAVE err: " + s ) }
		     }
		   );
	}
    );
    slave.start();

    sleep( 10000 );    
    
    print( "ADDING source" );
    connect( "127.0.0.1:" + slavePort + "/local" ).sources.save( { host : "127.0.0.1:" + masterPort , source:"main"} );
    
    sleep( 2000 );


    // --------
    
    // START REAL TEST
    
    print(" #####  SETUP COMPLETE : STARTING TEST  #####");

    arguments[0]( 
	connect( "127.0.0.1:" + masterPort + "/test" ) ,
	connect( "127.0.0.1:" + slavePort + "/test" ), 
	{ masterPort : masterPort,
	  slavePort : slavePort,
	  replTimeMS : 40000
    }
    );
    
    print(" #####  TEST COMPLETE : STARTING TEARDOWN  #####");
    
}
finally {
    // SHUTDOWN
    print( "SHUTTING DOWN" );
    sysexec( dbDir + "db msg end " + slavePort );
    slave.join();    
    
    sysexec( dbDir + "db msg end " + masterPort );
    master.join();
}


