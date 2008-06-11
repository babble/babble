/**
 *  API for the DB  class
 *
 *  possible @TODO - gt, lt, lte, gte
 *  
 * @fileOverview db.js - 10gen Database  Enhancement
 * @name 10gen Database DB Object API
 */
 
 /**
  * Database  utility API - adds functionality to standard native 10gen database connection
 *  @class 10gen Database Utility API
 */
var DB = function(){};

/**
  Create a new collection in the database.  Normally, collection creation is automatic.  You would
   use this function if you wish to specify special options on creation.

   If the collection already exists, no action occurs.
   
   <p>Options:</p>
   <ul>
   	<li>
     size: desired initial extent size for the collection.  Must be <= 1000000000.
           for fixed size (capped) collections, this size is the total/max size of the 
           collection.
    </li>
    <li>
     capped: if true, this is a capped collection (where old data rolls out).
    </li>
    <li> max: maximum number of objects if capped (optional).</li>
    </ul>

   <p>Example: </p>
   
   <code>createCollection("movies", { size: 10 * 1024 * 1024, capped:true } );</code>
 
 * @param {String} name Name of new collection to create 
 * @param {Object} options Object with options for call.  Options are listed above.
 * @return SOMETHING_FIXME
*/
DB.prototype.createCollection = function(name, opt) {
    var options = opt || {};
    var cmd = { create: name, capped: options.capped, size: options.size, max: options.max };
    var res = this._dbCommand(cmd);
    return res;
}


/**
 *  Returns the current profiling level of this database
 *  @return SOMETHING_FIXME or null on error
 */
 DB.prototype.getProfilingLevel  = function() { 
    var res = this._dbCommand( { profile: -1 } );
    return res ? res.was : null;
}

/**
 * <p> Set profiling level for your db.  Profiling gathers stats on query performance. </p>
 * 
 * <p>Default is off, and resets to off on a database restart -- so if you want it on,
 *    turn it on periodically. </p>
 *  
 *  <p>Levels :</p>
 *   <ul>
 *    <li>0=off</li>
 *    <li>1=log very slow (>100ms) operations</li>
 *    <li>2=log all</li>
 *  @param {String} level Desired level of profiling
 *  @return SOMETHING_FIXME or null on error
 */
DB.prototype.setProfilingLevel = function(level) {
    
    if (level < 0 || level > 2) { 
        throw { dbSetProfilingException : "input level " + level + " is out of range [0..2]" };        
    }
    
    if (level) {
	// if already exists does nothing
		this.createCollection("system.profile", { capped: true, size: 128 * 1024 } );
    }
    return this._dbCommand( { profile: level } );
}

/**
 *  <p> Evaluate a js expression at the database server.</p>
 * 
 * <p>Useful if you need to touch a lot of data lightly; in such a scenario
 *  the network transfer of the data could be a bottleneck.  A good example
 *  is "select count(*)" -- can be done server side via this mechanism.
 * </p>
 *
 * <p>
 * Use _dbEval() if you would like a return code for the evaluation.  _dbEval returns
 * { retval: functionReturnValue, ok: num [, errno: num] [, errmsg: str] }
 * </p>
 * 
 * <p>
 *  dbEval() simply returns the return value of the function that was invoked at the
 *  server.  If invocation fails (an exception occurs for example) null is returned.
 * </p>
 * <p>Example: </p>
 * <code>print( "count(*): " + dbEval( function(){db.mycoll.find({},{_id:ObjId()}).length();} );</code>
 *
 * @param {Function} jsfunction Javascript function to run on server 
 * @return result of your function, or null if error
 * 
 */
DB.prototype.dbEval = function(jsfunction) {
    var cmd = { $eval: jsfunction };
    if( arguments.length > 1 ) {
		cmd.args = arguments.slice(1);
    }
    var res = this._dbCommand( cmd );
    
    if (!res.ok) {
    	throw { dbEvalException : res };    	
    }
    
    return res.retval;
}




/**
 * 
 *  <p>
 *   Similar to SQL group by.  For example: </p>
 *
 *  <code>select a,b,sum(c) csum from coll where active=1 group by a,b</code>
 *
 *  <p>
 *    corresponds to the following in 10gen:
 *  </p>
 * 
 *  <code>
     db.group(
       {
         ns: "coll",
         key: { a:true, b:true },
	 // keyf: ...,
	 cond: { active:1 },
	 reduce: function(obj,prev) { prev.csum += obj.c; } ,
	 initial: { csum: 0 }
	 });
	 </code>
 *
 * 
 * <p>
 *  An array of grouped items is returned.  The array must fit in RAM, thus this function is not
 * suitable when the return set is extremely large.
 * </p>
 * <p>
 * To order the grouped data, simply sort it client side upon return.
 * <p>
   Defaults
     cond may be null if you want to run against all rows in the collection
     keyf is a function which takes an object and returns the desired key.  set either key or keyf (not both).
 * </p>
*/
DB.prototype.group = function(parmsObj) {
	
	var groupFunction = function() {
	    var parms = args[0];
    	var c = db[parms.ns].find(parms.cond||{});
    	var map = new Map();

    	while( c.hasNext() ) {
			var obj = c.next();

			var key = {};
			if( parms.key ) {
	    		for( var i in parms.key )
				key[i] = obj[i];
			}
			else {
	    		key = parms.$keyf(obj);
			}

			var aggObj = map[key];
			if( aggObj == null ) {
		    	var newObj = Object.extend({}, key); // clone
	    		aggObj = map[key] = Object.extend(newObj, parms.initial);
			}
			parms.$reduce(obj, aggObj);
		}

	    var ret = map.values();
   		return ret;
	}
	
    var parms = Object.extend({}, parmsObj);
    
    if( parms.reduce ) {
		parms.$reduce = parms.reduce; // must have $ to pass to db
		delete parms.reduce;
    }
    
    if( parms.keyf ) {
		parms.$keyf = parms.keyf;
		delete parms.keyf;
    }
    
    return this.dbEval(groupFunction, parms);
}

/* Run the specified database "command" object.
*/
DB.prototype._dbCommand = function( cmdObj ) {
    return this.$cmd.findOne(cmdObj);
}

var mydb  = arguments[0];

if (!mydb) throw "Error : no db object passed to enhance.";

Object.extend( mydb , DB.prototype );

return null;