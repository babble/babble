db = connect("kristina");
asdf = db.asdf;

var txt = "asdf";
for(var i=0; i<10; i++) {
    txt = txt + txt;
}

// fill db
for(var i=1; i<=5000; i++) {
    var obj = {txt : txt};
    asdf.save(obj);

    var obj2 = {txt: txt, comments: [{num: i, txt: txt}, {num: [], txt: txt}, {num: true, txt: txt}]};
    asdf.update(obj, obj2);

    if(i%100 == 0) {
        log("c: "+c+" i: "+i);
        var c = asdf.count();
        if(c != i) {
            log("DB: "+c+" i: "+i+"!");
            return;
        }
    }
}

