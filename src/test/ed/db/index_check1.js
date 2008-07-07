db = connect("ed_db_index_check1");

db.somecollection.drop();

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 0);

db.somecollection.save({a:1});

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 1);

db.somecollection.ensureIndex({a:1});

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 2);

db.somecollection.drop();

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 0);

db.somecollection.save({a:1});

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 1);

db.somecollection.ensureIndex({a:1});

//assert(db.system.namespaces.find({name:/somecollection/}).length() == 2);

print("ed/db/index_check1 : please remember to uncomment my asserts!");