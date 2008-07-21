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
import java.util.Arrays;
import java.util.Calendar;
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

    private static final String splitPatternFormat = "(\"(?:[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"|\'(?:[^\'\\\\]*(?:\\\\.[^\'\\\\]*)*)\'|[^%s]+)";
    
    public static String[] smart_split(String str) {
        return smart_split(str, "\\s");
    }
    
    public static String[] smart_split(String str, String splitChars) {
        String patternStr = String.format(splitPatternFormat, splitChars);
        Pattern pattern = Pattern.compile(patternStr);
        
        List<String> results = new ArrayList<String>();
        List<Character> quotes = Arrays.asList( new Character[] { '"', '\'' } );
        Matcher m = pattern.matcher(str);

        while(m.find()) {
            String bit = m.group(0);
            
            if(quotes.contains(bit.charAt(0)) && bit.charAt(bit.length() - 1) == bit.charAt(0)) {
                char quote = bit.charAt(0);
                
                bit = quote + bit.substring(1, bit.length() -1) + quote;
            }
            results.add(bit);
        }
        return results.toArray(new String[results.size()]);
    }
    public static String[] split(Pattern regex, String str, int maxsplit) {
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
        StringBuilder buffer = new StringBuilder();
        boolean escapeNext = false;

        for (int i = 0; i < format.length(); i++) {
            char c = format.charAt(i);

            if (c == '\\' && !escapeNext) {
                escapeNext = true;
                continue;
            }
            if (escapeNext) {
                buffer.append(c);
                escapeNext = false;
                continue;
            }

            DateFormatter formatter = map.get(c);
            if (formatter != null)
                buffer.append(formatter.format(date));
            else
                buffer.append(c);
        }

        return buffer.toString();
    }

    private static final Map<Character, DateFormatter> map = new HashMap<Character, DateFormatter>();
    static {
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
        // FIXME: time zone of the actual machine
        // map.put('T', new DateFormatter(null));

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
}
