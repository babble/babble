// JSData.java

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

package ed.js;

import java.util.*;
import java.text.*;

import ed.js.func.*;
import ed.util.*;
import ed.js.engine.*;

/** @expose */
public class JSDate extends JSObjectBase implements Comparable {

    /** @unexpose */
    public static JSFunction _cons =
        new JSFunctionCalls1(){

            public JSObject newOne(){
                return new JSDate();
            }

            public Object call( Scope s , Object foo , Object[] args ){

                if ( args != null && args.length > 0 ) {
                    int [] myargs = new int[8];
                    for( int i = 0; i < args.length+1 && i < myargs.length ; i++){
                        Object o = (i == 0 ? foo : args[i-1]);
                        if ( o instanceof JSString )
                            o = StringParseUtil.parseStrict(((JSString)o).toString());
                        if ( o instanceof Number )
                            myargs[i] = ((Number)o).intValue();
                    }
                    
                    Calendar c = Calendar.getInstance();
                    c.set( myargs[0], myargs[1], myargs[2], myargs[3], myargs[4], myargs[5] );
                    
                    c.set( c.MILLISECOND , myargs[6] );
                    
                    foo = new Long(c.getTimeInMillis());
                }


                Object o = s.getThis();
                if ( o == null || ! ( o instanceof JSDate ) )
                    return new JSString( (new JSDate( foo  )).toString() );

                JSDate d = (JSDate)o;
                long l = parse( foo , d._time );
                d._time = l;

                return d;
            }


            protected void init(){

                _prototype.set( "getTime" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            return ((JSDate)s.getThis())._time;
                        }
                    } );


                _prototype.set( "utc" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            return ((JSDate)s.getThis()).utc();
                        }
                    } );

                _prototype.set( "utc_to_local" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            return s.getThis();
                        }
                    } );

                _prototype.set( "next_month" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            JSDate d = (JSDate)s.getThis();
                            JSDate n = new JSDate( d._time );
                            n.setMonth( n.getMonth() + 1 );
                            return n;
                        }
                    } );

                _prototype.set( "last_month" , new JSFunctionCalls0() {
                        public Object call( Scope s , Object foo[] ){
                            JSDate d = (JSDate)s.getThis();
                            JSDate n = new JSDate( d._time );
                            n.setMonth( n.getMonth() - 1 );
                            return n;
                        }
                    } );

                _prototype.set( "strftime" , new JSFunctionCalls1() {
                        public Object call( Scope s , Object f , Object foo[] ){
                            return ((JSDate)s.getThis()).strftime( f.toString() );
                        }
                    } );

                _prototype.set( "valueOf" , new JSFunctionCalls0(){
                    public Object call( Scope s , Object args[] ){
                        return ((JSDate)s.getThis())._time;
                    }
                } );


                // -----------

                set( "now" , new JSFunctionCalls0(){
                        public Object call( Scope s, Object foo[] ){
                            return new JSDate();
                        }
                    } );

                set( "parse" , new JSFunctionCalls1(){
                        public Object call( Scope s , Object when , Object foo[] ){
                            long t = parse( when , -1 );
                            if ( t < 0 )
                                return null;
                            return new JSDate( t );
                        }
                    } );

                set( "DAYNAMES" , new JSArray( new JSString( "Sunday" ) ,
                                               new JSString( "Monday" ) ,
                                               new JSString( "Tuesday" ) ,
                                               new JSString( "Wednesday" ) ,
                                               new JSString( "Thursday" ) ,
                                               new JSString( "Friday" ) ,
                                               new JSString( "Saturday" )
                                               )
                     );

                set( "civil" , new JSFunctionCalls0(){
                        public Object call( Scope s, Object foo[] ){
                            // TODO: check this
                            return s.getThis();
                        }
                    } );


                /**
                 * @param function the function to call
                 * @param numTimes number of times to call function
                 * @param anything else gets passed to function
                 */
                set( "timeFunc" , new JSFunctionCalls2(){
                        public Object call( Scope s , Object func , Object numTimes , Object extra[] ){
                            if ( ! ( func instanceof JSFunction ) )
                                throw new RuntimeException( "Date.timeFunc needs a function" );

                            if ( ! ( numTimes instanceof Number ) )
                                throw new RuntimeException( "Date.timeFunc needs a number" );

                            JSFunction f = (JSFunction)func;
                            final int times = ((Number)numTimes).intValue();

                            final long start = System.currentTimeMillis();

                            for ( int i=0; i<times; i++ )
                                f.call( s , extra );

                            return System.currentTimeMillis() - start;
                        }
                    }
                    );
                

                _prototype.dontEnumExisting();
            }

        };

    /** Attempt to convert an object into a date.
     * @param o Object to be converted.
     * @return If successful, date in milliseconds equivalent to <tt>o</tt>. Otherwise the current time in milliseconds.
     */
    static long parse( Object o ){
        return parse( o , System.currentTimeMillis() );
    }

    /** Attempt to convert an object into a date.
     * @param o Object to be converted.
     * @param def Default time in milliseconds to be returned if <tt>o</tt> is unparsable.
     * @return If successful, date in milliseconds equivalent to <tt>o</tt>. Otherwise <tt>def</tt>.
     */
    static long parse( Object o , long def ){

        if ( o == null )
            return def;

        if ( o instanceof Date )
	    return ((Date)o).getTime();

        if ( o instanceof String || o instanceof JSString )
            return parseDate( o.toString() , def );

        if ( ! ( o instanceof Number ) )
            return def;
        return ((Number)o).longValue();
    }

    /** Attempt to convert a string into a date.
     * @param s String to be converted.
     * @param def Default time in milliseconds to be returned if <tt>o</tt> is unparsable.
     * @return If successful, date in milliseconds equivalent to <tt>o</tt>. Otherwise <tt>def</tt>.
     */
    public static long parseDate( String s , long def ){
        if ( s == null )
            return def;
        s = s.trim();
        if ( s.length() == 0 )
            return def;

        for ( int i=0; i<DATE_FORMATS.length; i++ ){
            try {
                synchronized ( DATE_FORMATS[i] ) {
                    return DATE_FORMATS[i].parse( s ).getTime();
                }
            }
            catch ( java.text.ParseException e ){
            }
        }

        return def;
    }

    /** Initializes a new date object to the current time. */
    public JSDate(){
        this( System.currentTimeMillis() );
    }

    /** Initializes a new date object to a time given in milliseconds.
     * @param t Time, in milliseconds, to initialize date as.
     */
    public JSDate( long t ){
        super( _cons );
        _time = t;
    }

    /** Initializes a new date object using a given Calendar.
     * @param c Calendar to use to set date.
     */
    public JSDate( Calendar c ){
        this( c.getTimeInMillis() );
    }

    /** Initializes a new date object to a value determined by attempting to parse a given object.
     * @param foo Object to be parsed.
     */
    public JSDate( Object foo ){
        this( parse( foo ) );
    }

    /** Return this date's time in milliseconds.
     * @return Time in milliseconds.
     */
    public long getTime(){
        return _time;
    }

    /** Return this date's year.  If the year is between -1 and 200 (exculsive), the year returned is 1900 added to this date's year.
     * @return This date's year.
     */
    public int getYear(){
        _cal();
        int y = _c.get( Calendar.YEAR );
        if ( y >= 0 && y < 200 )
            return 1900 + y;
        return y;
    }

    /** This date's full four-digit year.
     * @return This date's year.
     */
    public int getFullYear() {
        _cal();
        return _c.get( Calendar.YEAR );
    }

    /** The month field of this date, which ranges from 0 (January) to 11 (December).
     * @return The month field.
     */
    public int getMonth(){
        _cal();
        return _c.get( Calendar.MONTH );
    }

    /** The day of the week field of this date, which ranges from 0 (Sunday) to 6 (Saturday).
     * @return The day field.
     */
    public int getDay(){
        _cal();
        return _c.get( Calendar.DAY_OF_WEEK );
    }

    /** The hours field of this date, which ranges from 0 (midnight) to 23 (11pm).
     * @return The hours field.
     */
    public int getHours() {
        _cal();
        return _c.get( Calendar.HOUR_OF_DAY );
    }

    /** The milliseconds field of this date..
     * @return The milliseconds field.
     */
    public int getMilliseconds() {
        _cal();
        return _c.get( Calendar.MILLISECOND );
    }

    /** The seconds field of this date, which ranges from 0 to 59.
     * @return The seconds field.
     */
    public int getSeconds() {
        _cal();
        return _c.get( Calendar.SECOND );
    }

    /** The day of the month specified by this date, which ranges from 1 to 31.
     * @return The day of the month field.
     */
    public int getDate() {
        _cal();
        return _c.get( Calendar.DAY_OF_MONTH );
    }

    /** The hours field of this date, which ranges from 0 to 23.
     * @return The hours field.
     */
    public int getHourOfDay(){
        _cal();
        return _c.get( Calendar.HOUR_OF_DAY );
    }

    /** The minutes field of this date, which ranges from 0 to 59.
     * @return The minutes field.
     */
    public int minutes(){
        return getMinute();
    }

    /** The minutes field of this date, which ranges from 0 to 59.
     * @return The minutes field.
     */
    public int getMinutes(){
        return getMinute();
    }

    /** The minutes field of this date, which ranges from 0 to 59.
     * @return The minutes field.
     */
    public int getMinute(){
        _cal();
        return _c.get( Calendar.MINUTE );
    }

    /** Set the day of the month field.  Valid values are between 1 and 31.
     * @param day Day of the month.
     * @return New time in milliseconds.
     */
    public long setDate(int day) {
        _cal();
        _c.set( Calendar.DATE, day);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the year, month, and day.
     * @param year
     * @param month
     * @param day
     * @return New time in milliseconds.
     */
    public long setFullYear(int year, int month, int day) {
        setMonth(month);
        setDate(day);
        return setFullYear(year);
    }

    /** Set the year and month.
     * @param year
     * @param month
     * @return New time in milliseconds.
     */
    public long setFullYear(int year, int month) {
        setMonth(month);
        return setFullYear(year);
    }

    /** Set the year.
     * @param year
     * @return New time in milliseconds.
     */
    public long setFullYear(int year) {
        _cal();
        _c.set( Calendar.YEAR, year );
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the hour, minute, second, and millisecond.
     * @param hour
     * @param min
     * @param sec
     * @param ms
     * @return New time in milliseconds
     */
    public long setHours(int hour, int min, int sec, int ms) {
        setMinutes(min);
        setSeconds(sec);
        setMilliseconds(ms);
        return setHours(hour);
    }

    /** Set the hour, minute and second.
     * @param hour
     * @param min
     * @param sec
     * @return New time in milliseconds
     */
    public long setHours(int hour, int min, int sec) {
        setMinutes(min);
        setSeconds(sec);
        return setHours(hour);
    }

    /** Set the hour and minute.
     * @param hour
     * @param min
     * @return New time in milliseconds
     */
    public long setHours(int hour, int min) {
        setMinutes(min);
        return setHours(hour);
    }

    /** Set the hour.
     * @param hour
     * @return New time in milliseconds
     */
    public long setHours(int hour) {
        _cal();
        _c.set( Calendar.HOUR_OF_DAY, hour);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the milliseconds.
     * @param ms
     * @return New time in milliseconds
     */
    public long setMilliseconds(int ms) {
        _cal();
        _c.set( Calendar.MILLISECOND, ms);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the minutes, seconds, and milliseconds.
     * @param min
     * @param sec
     * @param ms
     * @return New time in milliseconds
     */
    public long setMinutes(int min, int sec, int ms) {
        setSeconds(sec);
        setMilliseconds(ms);
        return setMinutes(min);
    }

    /** Set the minutes and seconds.
     * @param min
     * @param sec
     * @return New time in milliseconds
     */
    public long setMinutes(int min, int sec) {
        setSeconds(sec);
        return setMinutes(min);
    }

    /** Set the minutes.
     * @param min
     * @return New time in milliseconds
     */
    public long setMinutes(int min) {
        _cal();
        _c.set( Calendar.MINUTE, min );
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the month and day.
     * @param month
     * @param day
     * @return New time in milliseconds
     */
    public long setMonth(int month, int day) {
        setDate(day);
        return setMonth(month);
    }

    /** Set the month.
     * @param month
     * @return New time in milliseconds
     */
    public long setMonth(int month) {
        _cal();
        _c.set(Calendar.MONTH, month);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the year.
     * @param year
     * @return New time in milliseconds
     */
    public long setYear(int year) {
        _cal();
        _c.set(Calendar.YEAR, year);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the seconds and milliseconds
     * @param sec
     * @param ms
     * @return New time in milliseconds
     */
    public long setSeconds(int sec, int ms) {
        setMilliseconds(ms);
        return setSeconds(sec);
    }

    /** Set the seconds
     * @param sec
     * @return New time in milliseconds
     */
    public long setSeconds(int sec) {
        _cal();
        _c.set(Calendar.SECOND, sec);
        _time = _c.getTimeInMillis();
        return _c.getTimeInMillis();
    }

    /** Set the milliseconds
     * @param ms
     * @return New time in milliseconds
     */
    public long setTime(long ms) {
        _cal();
        _c.setTimeInMillis(ms);
        return _time = _c.getTimeInMillis();
    }

    /** Return this date as a string of the form "EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)"
     * @return A string representation of this date.
     */
    public String toString(){
        return format( _defaultFormat );
    }

    /** Return this date as a string in a given format. Uses strftime.
     * @param theFormat Format to use.
     * @return The formatted date string.
     */
    public String strftime( String theFormat ){
        return format( ed.util.Strftime.convertDateFormat( theFormat ) );
    }

    /** Return this date as a string in a given format.
     * @param theFormat Format to use.
     * @return The formatted date string.
     */
    public String format( String theFormat ){
        return format( theFormat , TimeZone.getDefault() );
    }

    /** Return this date as a string in a given format.
     * @param theFormat Format to use.
     * @param tz Timezone
     * @return The formatted date string.
     */
    public String format( String theFormat , String tz ){
        return format( theFormat , TimeZone.getTimeZone( tz ) );
    }

    /** Return this date as a string in a given format.
     * @param theFormat Format to use.
     * @param tz Timezone
     * @return The formatted date string.
     */
    public String format( String theFormat , TimeZone tz ){
        SimpleDateFormat df = new SimpleDateFormat( theFormat );
        df.setTimeZone( tz );
        return df.format( new Date( _time ) );
    }

    /** Return this date as a string in web format, defined as "EEE, dd MMM yyyy HH:mm:ss z".
     * @return The formatted date string.
     */
    public String webFormat(){
        return format( _webFormat );
    }

    /** Return this date as a string in simple format, defined as "MM/dd/yyyy HH:mm".
     * @return The formatted date string.
     */
    public String simpleFormat(){
        return format( _simpleFormat );
    }

    /** Return this date as a string in a given format using the default timezone.
     * @param df The date format to use.
     * @return The formatted date string.
     */
    public String format( DateFormat df ){
        return format( df , TimeZone.getDefault() );
    }

    /** Return this date as a string in a given format using a given timezone.
     * @param df The date format to use.
     * @param tz The timezone to use.
     * @return The formatted date string.
     */
    public String format( DateFormat df , String tz ){
        return format( df , TimeZone.getTimeZone( tz ) );
    }

    /** Return this date as a string in a given format using a given timezone.
     * @param df The date format to use.
     * @param tz The timezone to use.
     * @return The formatted date string.
     */
    public String format( DateFormat df , TimeZone tz ){

        if ( tz == null )
            tz = TimeZone.getDefault();

        synchronized ( df ){
            df.setTimeZone( tz );
            return df.format( new Date( _time ) );
        }
    }

    /** Returns the first day of the month this date's month field represents.
     * @return A Date at the beginning of this date's month.
     */
    public JSDate roundMonth(){
        return new JSDate( _roundMonth() );
    }

    /** Returns the first day of the week this date's week field represents.
     * @return A Date at the beginning of this date's week.
     */
    public JSDate roundWeek(){
	return new JSDate( _roundWeek() );
    }

    /** Returns the first hour of the day this date's day field represents.
     * @return A Date at the beginning of this date's day.
     */
    public JSDate roundDay(){
        return new JSDate( _roundDay() );
    }

    /** Returns the first minute, seconds, and millisecond of the hour this date's hour field represents.
     * @return A Date at the beginning of this date's hour.
     */
    public JSDate roundHour(){
        return new JSDate( _roundHour() );
    }

    /** @unexpose */
    public Calendar _roundMonth(){
        Calendar c = _roundDay();
        c.set( c.DAY_OF_MONTH , 1 );
        return c;
    }

    /** @unexpose */
    public Calendar _roundWeek(){
	Calendar c = _roundDay();
	while ( c.get( c.DAY_OF_WEEK ) != c.MONDAY )
	    c.setTimeInMillis( c.getTimeInMillis() - ( 1000 * 60 * 60 * 24 ) );
	return c;
    }

    /** @unexpose */
    public Calendar _roundDay(){
        Calendar c = _roundHour();
        c.set( c.HOUR_OF_DAY , 0 );
        return c;
    }

    /** @unexpose */
    public Calendar _roundHour(){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( _time );
        c.set( c.MILLISECOND , 0 );
        c.set( c.SECOND , 0 );
        c.set( c.MINUTE , 0 );
        return c;
    }

    /** Returns the first second and millisecond of the minute this date's minute field represents.
     * @return A Date at the beginning of this date's minute.
     */
    public JSDate roundMinutes( int min ){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( _time );
        c.set( c.MILLISECOND , 0 );
        c.set( c.SECOND , 0 );

        double m = c.get( c.MINUTE );
        m = m / min;

        c.set( c.MINUTE , min * (int)m );

        return new JSDate( c );
    }

    /**
     * As required by the spec.  Spec apparently gives us freedom for format. Silly spec.
     * @return value of date object in UTC
     */
    public String toUTCString() {
    	synchronized(_utcFormat) {
    		return _utcFormat.format( new Date( _time ) );
    	}
    }

    /** Returns this date objct.
     * @return This date.
     */
    public JSDate utc(){
        return this;
    }

    /** @unexpose */
    private void _cal(){
        if ( _c != null )
            return;
        _c = Calendar.getInstance();
        _c.setTimeInMillis( _time );
    }

    /** Compare an object with this date.
     * @param o Object to compare with this date.
     * @return If the object is a date and is earlier than this date, return 1.  If the object is later, return -1, if they are the same date, return 0.
     */
    public int compareTo( Object o ){
        long t = -1;
        if ( o instanceof JSDate )
            t = ((JSDate)o)._time;

        if ( t < 0 )
            return 0;

        long diff = _time - t;
        if ( diff == 0 )
            return 0;
        if ( diff < 0 )
            return -1;
        return 1;
    }

    /** This date's hash code.
     * @return The hash code for this date.
     */
    public int hashCode(){
        return (int)_time;
    }

    /** Determine if this date is equal to a given object.
     * @param o Object to compare.
     * @return If the object is an equivelent date to this date.
     */
    public boolean equals( Object o ){
        return
            o instanceof JSDate &&
            _time == ((JSDate)o)._time;
    }

    /** @unexpose */
    long _time;
    /** @unexpose */
    Calendar _c;

    /** @unexpose */
    public static final DateFormat _defaultFormat = new SimpleDateFormat( "EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)" );

    /** @unexpose */
    public static final DateFormat _simpleFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    /** @unexpose */
    public static final DateFormat _webFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
    /** @unexpose */
    public static final DateFormat _utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
	_webFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
	_utcFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /** Default date formats */
    private final static DateFormat[] DATE_FORMATS = new DateFormat[]{
        _webFormat , _simpleFormat ,
        new SimpleDateFormat( "yyyy-dd-MM HH:mm:ss z" ) ,
        new SimpleDateFormat( "yyyy-dd-MM HH:mm: z" ) ,
        new SimpleDateFormat( "yyyy-dd-MM HH:mm:ss" ) ,
        new SimpleDateFormat( "yyyy-dd-MM HH:mm" ) ,
        new SimpleDateFormat( "dd/MMM/yyyy:HH:mm:ss" )
    };

}
