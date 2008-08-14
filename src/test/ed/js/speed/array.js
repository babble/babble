function go() { 
    
    var d = new Date();
    var i=0;
    var arr = [];
    
    for (i = 0; i < 20000; i++) { 
	arr[i] = i;
    }
    
    for (i = 0; i < 20000; i++) { 
	arr.shift();
    }
    
    for (i = 0; i < 20000; i++) { 
	arr[i] = i;
    }
    
    for (i = 0; i < 20000; i++) { 
	arr.pop();
    }
    
    
    var e = new Date();
    
    print(e-d);
}


//while(1)
go();
go();
