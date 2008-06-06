package ed.appserver.templates.djang10;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class Util {
    public static int countOccurance(CharSequence str, char character) {
        int count = 0;

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == character)
                count++;
        }

        return count;
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
