function threeMonthSummary(limit, t) {

    data_i = [];
    data_o = [];
    millis = [];


    var sum = [];
    var i = 0;
    total = { b_in : 0, b_out: 0, io : 0, millis: 0 };

    data_i.forEach( function( b_in ){ sum[i++] = b_in.num; } );
    i = 0;
    data_o.forEach( function( b_out ){ sum[i++] += b_out.num; } );

    for(i = 0;  i<3; i++) {
	m_index =  (month-1-i < 0) ? ( month + (12 - (i+1)) ) : (month - 1 - i);
    }


}
