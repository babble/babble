
/**

db.git
 { name : "corejs" , 
   branches : { 
     master : "abc" ,
     www    : "def"
   }
 }

*/

Cloud.Git = {};

Cloud.Git.ensureHash = function( repos , branch , hash ){
    log.git.debug( "ensureHash " + repos + "/" + branch + "/" + hash );
};

