/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
