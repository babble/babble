
o = {
    a : 1 ,
    b : "hello" , 
    c : [ 1 , 2 ] ,
    d : true ,
    e : false , 
    f : /abc/g
};

assert.eq( o.hashCode() , fromjson( tojson( o ) ).hashCode() );


o = {
    _id : ObjectId() ,
    a : 1 ,
    b : "hello" , 
    c : [ 1 , 2 ] , 
    //d : new Date()
};

assert.eq( o.hashCode() , fromjson( tojson( o ) ).hashCode() );


