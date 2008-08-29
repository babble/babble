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

package ed.appserver.templates.djang10;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    public static int countOccurance(CharSequence str, char character) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == character)
                count++;
        }

        return count;
    }

    public static String[] smart_split(String str) {
        return smart_split(str, new String[]{" ", "\r", "\n", "\t"}, false);
    }

    public static String[] smart_split(String str, String[] splitStrs, boolean allowEmpty) {
        List<String> results = new ArrayList<String>();
        
        str = str.trim();
        boolean inQuote = false, escapeNext = false;
        char quoteChar = '"';
        int parenNesting = 0, bracketNesting = 0;
        
        int start = 0;
        for(int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            
            if(escapeNext) {
                escapeNext = false;
            }
            else if(inQuote) {
                if(c == '\\')
                    escapeNext = true;
                else if(c == quoteChar)
                    inQuote = false;
            }
            else {
                switch(c){
                case '"':
                case '\'':
                    inQuote = true;
                    quoteChar = c;
                    break;
                case '(':
                    parenNesting++;
                    break;
                case ')':
                    parenNesting--;
                    break;
                case '[':
                    bracketNesting++;
                    break;
                case ']':
                    bracketNesting--;
                    break;
                default:
                    if(parenNesting != 0 || bracketNesting != 0)
                        continue;
                    
                    String splitter = null;
                    for(String splitStr : splitStrs) {
                        if(str.regionMatches(i, splitStr, 0, splitStr.length())) {
                            splitter = splitStr;
                            break;
                        }
                    }
                    if(splitter == null)
                        continue;
                    
                    if(allowEmpty || i - start > 0)
                        results.add(str.substring(start, i));
                    
                    start = i + splitter.length();
                    i = start - 1;                    
                }
            }
        }
        if(start < str.length())
            results.add(str.substring(start));
            
        return results.toArray(new String[results.size()]);
    }
    
    public static String[] split(Pattern regex, String str) {
        List<String> parts = new ArrayList<String>();
        Matcher m = regex.matcher(str);
        
        int lastI = 0;
        
        while(m.find()) {
            parts.add(str.substring(lastI, m.start()));
            for(int i = 1; i < m.groupCount() + 1; i++)
                parts.add(m.group(i));
            
            lastI = m.end();
        }
        parts.add(str.substring(lastI));
        
        return parts.toArray(new String[parts.size()]);
    }
    
    public static String escape(String pattern) {
        StringBuilder buffer = new StringBuilder();
        for(int i=0; i<pattern.length(); i++) {
            char c = pattern.charAt(i);
            if(!Character.isLetterOrDigit(c))
                buffer.append("\\");
            
            buffer.append(c);
        }
        return buffer.toString();
    }
    
    public static String formatDate(Date date, String format) {
        return _formatDateTime(date, format, DATE_FORMAT_MAP);
    }
    public static String formatTime(Date time, String format) {
        return _formatDateTime(time, format, TIME_FORMAT_MAP);
    }
    
    private static String _formatDateTime(Date dateTime, String format, Map<Character, DateFormatter> formatMap) {
        StringBuilder buffer = new StringBuilder();
        boolean escapeNext = false;

        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            if (escapeNext) {
                buffer.append(c);
                escapeNext = false;
                continue;
            }
            if (c == '\\') {
                escapeNext = true;
                continue;
            }

            DateFormatter formatter = formatMap.get(c);
            if (formatter != null)
                buffer.append(formatter.format(dateTime));
            else
                buffer.append(c);
        }

        return buffer.toString();
    }

    private static final Map<Character, DateFormatter> DATE_FORMAT_MAP;
    static {
        final Map<Character, DateFormatter> map = new HashMap<Character, DateFormatter>();
        DATE_FORMAT_MAP = Collections.unmodifiableMap(map);

        map.put('a', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                return getCalendar(date).get(Calendar.AM_PM) == Calendar.AM ? "a.m." : "p.m.";
            }
        });
        map.put('A', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                return getCalendar(date).get(Calendar.AM_PM) == Calendar.AM ? "AM." : "PM";
            }
        });
        map.put('b', new DateFormatter("MMM") {
            @Override
            public String format(Date date) {
                return super.format(date).toLowerCase();
            }
        });
        map.put('d', new DateFormatter("dd"));
        map.put('D', new DateFormatter("EEE"));
        map.put('f', new DateFormatter("h:mm") {
            @Override
            public String format(Date date) {
                String time = super.format(date);
                if (getCalendar(date).get(Calendar.MINUTE) == 0)
                    time = time.substring(0, time.length() - 3);
                return time;
            }
        });
        map.put('F', new DateFormatter("MMMM"));
        map.put('g', new DateFormatter("h"));
        map.put('G', new DateFormatter("H"));
        map.put('h', new DateFormatter("hh"));
        map.put('H', new DateFormatter("HH"));
        map.put('i', new DateFormatter("mm"));
        map.put('j', new DateFormatter("d"));
        map.put('l', new DateFormatter("EEEE"));
        map.put('L', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                GregorianCalendar cal = getCalendar(date);
                int year = cal.get(Calendar.YEAR);

                return Boolean.toString(cal.isLeapYear(year));
            }
        });
        map.put('m', new DateFormatter("MM"));
        map.put('M', new DateFormatter("MMM"));
        map.put('n', new DateFormatter("M"));
        map.put('N', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                return apNames[getCalendar(date).get(Calendar.MONTH)];
            }
        });
        map.put('O', new DateFormatter("Z"));
        map.put('P', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                GregorianCalendar cal = getCalendar(date);
                if (cal.get(Calendar.MINUTE) == 0) {
                    if (cal.get(Calendar.HOUR_OF_DAY) == 0)
                        return "midnight";

                    else if (cal.get(Calendar.HOUR_OF_DAY) == 12)
                        return "noon";
                }

                return map.get('f').format(date) + " " + map.get('a').format(date);
            }
        });
        map.put('r', new DateFormatter("EEE, d MMM yyyy HH:mm:ss Z"));
        map.put('s', new DateFormatter("ss"));
        map.put('S', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                int day = getCalendar(date).get(Calendar.DAY_OF_MONTH);

                if (day == 11 || day == 12 || day == 13)
                    return "th";

                switch (day % 10) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                default:
                    return "th";
                }
            }
        });
        map.put('t', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                return Integer.toString(getCalendar(date).getActualMaximum(Calendar.DAY_OF_MONTH));
            }
        });
        map.put('T', new DateFormatter("z"));

        map.put('w', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                return Integer.toString(getCalendar(date).get(Calendar.DAY_OF_WEEK) - 1);
            }
        });
        map.put('W', new DateFormatter(null) {
            @Override
            public String format(Date date) {
                GregorianCalendar cal = getCalendar(date);
                cal.setFirstDayOfWeek(Calendar.MONDAY);

                return Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
            }
        });

        map.put('y', new DateFormatter("yy"));
        map.put('Y', new DateFormatter("yyyy"));
        map.put('z', new DateFormatter("D"));

        map.put('Z', new DateFormatter(null) {
            public String format(Date arg0) {
                //FIXME: read env variable for the time zone
                return "" + ((Calendar.getInstance().get(Calendar.ZONE_OFFSET) + Calendar.getInstance().get(Calendar.DST_OFFSET))/1000);
            }
        });
        // FIXME: time zone in secs
        /*
         * map.put('Z', new DateFormatter(null) { @Override public String
         * format(Date date) {
         * 
         * return super.format(date); } })
         */

    }

    private static final String[] apNames = new String[] { "Jan.", "Feb.", "March", "April", "May", "June", "July", "Aug.",
            "Sept.", "Oct.", "Nov.", "Dec." };

    private static class DateFormatter {
        private final SimpleDateFormat format;

        public DateFormatter(String formatStr) {
            this.format = formatStr == null ? null : new SimpleDateFormat(formatStr);
        }

        public String format(Date date) {
            return format.format(date);
        }

        protected GregorianCalendar getCalendar(Date date) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return calendar;
        }
    }
    
    
    private static final Map<Character, DateFormatter> TIME_FORMAT_MAP;
    static {
        Map<Character, DateFormatter> map = new HashMap<Character, DateFormatter>();
        TIME_FORMAT_MAP = Collections.unmodifiableMap(map);
        
        char[] similarities = { 'a', 'A', 'f', 'g', 'G', 'h', 'H', 'i', 'P', 's' };
        for(char similarity : similarities)
            map.put(similarity, DATE_FORMAT_MAP.get(similarity));
        
        map.put('B', new DateFormatter(null) {
            public String format(Date arg0) {
                throw new UnsupportedOperationException();
            }
        });
    }
    
    
}
