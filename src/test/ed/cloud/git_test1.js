

assert.eq( "a,b" , [ "a" , "b" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "a,b" , [ "b" , "a" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "a,a" , [ "a" , "a" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );

assert.eq( "r0,r1" , [ "r1" , "r0" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "r0,r1" , [ "r0" , "r1" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );

assert.eq( "r1,r10" , [ "r1" , "r10" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "r1,r10" , [ "r10" , "r1" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );

assert.eq( "r1.0,r1.1" , [ "r1.0" , "r1.1" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "r1.0,r1.1" , [ "r1.1" , "r1.0" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );


assert.eq( "r1a,r1b" , [ "r1a" , "r1b" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
assert.eq( "r1a,r1b" , [ "r1b" , "r1a" ].sort( Cloud.Git.tagNameSortFunc ).toString()  );
