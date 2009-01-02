// balancer_test4.js

assert( Cloud.Balancer.getAvailableDB() );
assert( Cloud.Balancer.getAvailablePool() );

assert.eq( "string" , typeof( Cloud.Balancer.getAvailableDB() ) );
assert.eq( "string" , typeof( Cloud.Balancer.getAvailablePool() ) );
