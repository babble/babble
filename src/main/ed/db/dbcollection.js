/**
 *  API for DB Collection objects
 *
 *  
 * @fileOverview dbutil.js - 10gen Database Collection  API
 * @name 10gen Database Collection API 
 */

 /**
  * Database collection  API
 *  @class 10gen Database Collection  API
 */
 var DBCollection = function(){}

/**
 *  <p>Drop all indexes on the specified collection.</p>
 * 
 *  <p>Note : alpha: space is not reclaimed</p>
 *
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.dropIndexes = function() {
    var res = this._dbCommand( { deleteIndexes: this.getName(), index: "*" } );
    if( res && res.ok && res.ok == 1 ) {
        this.getDB().system.indexes.remove( { ns: this.getFullName() } );
		this.getDB().system.namespaces.remove( { name: RegExp(this.getFullName() + "[.][$].*") } );
    }
    
    this.resetIndexCache();
    
    return res;
}

/**
 * <p>Drop a specified index.</p>
 *
 * <p>
 * Name is the name of the index in the system.indexes name field. (Run db.system.indexes.find() to
 *  see example data.)
 * </p>
 *
 * <p>Note :  alpha: space is not reclaimed </p>
 * @param {String} name of index to delete.
 * @return SOMETHING_FIXME
 */
 DBCollection.prototype.dropIndex =  function(index) {
    assert(index);
    
    if ( ! isString( index ) && isObject( index ) )
    	index = this.genIndexName( index );
    
    var res = this._dbCommand( { deleteIndexes: this.getName(), index: index } );
    if( res && res.ok && res.ok == 1 ) {
        this.getDB().system.indexes.remove( { ns: this.getFullName(), name: index } );
		this.getDB().system.namespaces.remove( { name: this.getFullName() + ".$"+index } );
    }
    
    this.resetIndexCache();
    
    return res;
}
 
/** 
 * Validate the data in a collection, returning some stats.
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.validate = function() {
    return this._dbCommand( { validate: this.getName() } );
}

/** 
 * <p>drops all objects in the collection</p>
 * <p>Note :  alpha: space not reclaimed.</p>
 * 
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.drop = function()
{
    var res = this._dbCommand( { drop: this.getName() } );
    if( res && res.ok && res.ok == 1 ) {
        this.getDB().system.indexes.remove( { ns: this.getFullName() } );
		this.getDB().system.namespaces.remove( { name: this.getFullName() } );
		this.getDB().system.namespaces.remove( { name: RegExp(this.getFullName() + "[.][$].*") } );
    }
    return res;
}

/**
 *  Drop free lists. Normally not used.
 */
DBCollection.prototype.clean = function() {
    return this._dbCommand( { clean: this.getName() } );
}


/**
 *  <p>
 *  count # of objects in a collection
 * </p>
 *
 *  <p>  You can specify a filter for counting through the optional  parameter.
 *      This parameter specifies condition that must be true for the objects to
 *        be counted.  
 *  </p>
 *  <p>Example:</p>
 * 
 *   <code>c = count({active:true});</code>
 * 
 * @param {Object} query Optional query to use to filter objects for counting
 * @return number of objects in the collection that optionally match the filter condition
 */
DBCollection.prototype.count = function(query) {
	
	var countFunction = function() { 
		return db[args[0]].find(args[1]||{}, {_id:ObjId()}).length();
	}
   return this.getDB().dbEval(countFunction, this.getName(), query);
}

/* Run the specified database "command" object.
*/
DBCollection.prototype._dbCommand = function( cmdObj ) {
    return this.getDB().$cmd.findOne(cmdObj);
}

DBCollection.prototype.sample = function( num , query ){
    var r = this.getDB().dbEval(
        function(){
            var a = db[args[0]].find().limit(10000).toArray();
            a.shuffle();
            return a.splice( 0 , args[1] );
        } ,
        this.getName() , num , query 
    );
    var a = [];
    for ( var k in r ){
        a.push( r[k] );
    }
    return a;
}

var mydb  = arguments[0];

if (!mydb) {
       throw "Error : no db object passed to augmentor for Collection";
}

Object.extend( mydb.getCollectionPrototype() , DBCollection.prototype );

return null;

//
//  other stuff
//


/**
 *  RESET - FOLLOWING IS FOR DOCUMENTATION PURPOSES ONLY
 */
DBCollection.prototype = {};

/**
 * <p>Returns the name of the collection.</p>
 *
 * @return {String} Name of the collection
 */
DBCollection.prototype.name = function() {
    throw {exception : "Native Call name() : shouldn't have JS implementation"};
}


//       _entries.put( "base" , _base.getName() );
 
 /**
 * <p>
 * Saves an object in the collection, or update if the object (as identified by _id)
 * is already there.
 * </p>
 *
 * <p>
 * If your object has a <code>presave</code> $TODO_DOC method, that method will be called 
 * before the object is saved to the db (before both updates and inserts).
 * </p>
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.save = function(obj) {
    throw {exception : "Native Call save() : shouldn't have JS implementation"};
}

 
 /**
 * <p>
 * Update an object in the database, based on supplied query criteria and options.  Normally
 * you should just use the <code>save()</code> method.
 * </p>
 * <p>
 * Current options include : 
 * </p>
 * <ul>
 * <li>upsert:true - perform an update if exists, or insert new if doesn't exist</li>
 * <li>ids : false - do not add an object _id field</li>
 * </ul>
 * 
 * <p>
 * Example : 
 * </p>
 * <code>db.myColl.update( { name: "Joe" }, { name: "Joe", age: 20 }, { upsert: true } );</code>
 * 
 * <p> @see  http://admin.10gen.com/wiki/pub.db.update </p>
 * 
 * @param {Object} criteria  query which selects the record @TODO{more than one?} to update
 * @param {Object} obj  updated object 
 * @param {Object} options  optional options object to use in the update
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.update = function(criteria, obj, options) {
    throw {exception : "Native Call update() : shouldn't have JS implementation"};
}

 /**
 * <p>
 * Removes all objects from the collection that match the optional criteria.
 * </p>
 * <p>
 * Example : 
 * </p>
 * <code>db.myColl.remove( { name: "Joe" });</code>
 * 
 * @param {Object} criteria  query which selects the records  to be removed from the collection
 * @return SOMETHING_FIXME
 */
DBCollection.prototype.remove = function(criteria) {
    throw {exception : "Native Call remove() : shouldn't have JS implementation"};
}


/**
 *   <p>
 *    @TODO - does something
 *   </p>
 * 
 *   @param {Object} obj obj to apply
 *   @return SOMETHING_FIXME
 */
DBCollection.prototype.apply = function(obj) {
    throw {exception : "Native Call apply() : shouldn't have JS implementation"};
}

/**
 *   <p>
 *    Finds one or more objects in the collection based on the optional specified criteria.
 *   </p>
 * 
 *   <p>
 *   Example :
 *   </p>
 *   <code>db.blogposts.find();</code>
 *   <br/>
 *   <code>db.blogposts.find({title : /10gen/} );</code>
 *   <p>
 *   In addition to the criteria, a field selector may be applied to limit the fields that
 *   are returned in the objects.
 *   </p>
 *   <p>
 *   Example :
 *   </p>
 *   <code>db.blogposts.find({}, {comments:true});</code>
 *   
 *   <p>
 *   The Cursor returned by find can have modifiers applied for more advanced functionalty.
 *   See @see $TODO-LINK_TO_CURSOR_DOC for more informatiion.
 *   </p>
 *   @param {Object} criteria Optional criteria for selection of object(s) to be returned
 *   @param {Object} selector Optional selector to specify the fields that should be returned in each object
 *   @return {Cursor} Cursor for the found object set
 */
DBCollection.prototype.find = function(criteria, selector) {
    throw {exception : "Native Call find() : shouldn't have JS implementation"};
}

/**
 *   <p>
 *    Finds a single objects in the collection based on the specified criteria.
 *   </p>
 * 
 *   <p>
 *   Example :
 *   </p>
 *   <code>db.blogposts.findOne({title : /10gen/} );</code>
 *   @param {Object} criteria Criteria for selection of object to be returned
 *   @return {Object} Found object or null if no objects match the criteria
 */
DBCollection.prototype.findOne = function(criteria) {
    throw {exception : "Native Call findOne(): shouldn't have JS implementation"};
}

/**
 *   <p>
 *    Returns an array of the indexes for the collection.
 *   </p>
 *   <p>
 *   Each entry in the array is an index descriptor object than contains the 
 *   following properties :  
 *   </p>
 *   <ul>
 *   <li> name : name of the index</li>
 *   <li> ns : name of the namespace the index is in</li>
 *   <li> key : object property that's being indexed<li>
 *   </ul>
 * 
 *   @return {Array} Array of index descriptor objects
 */
DBCollection.prototype.getIndexes = function() {
    throw {exception : "Native Call getIndexes() : shouldn't have JS implementation"};
}


/**
 *   <p>
 *    Returns a JSON representation of the collection.
 * 
 *   @return {String} JSON representation of the collection
 */
DBCollection.prototype.tojson = function() {
    throw {exception : "Native Call tojson() : shouldn't have JS implementation"};
}
