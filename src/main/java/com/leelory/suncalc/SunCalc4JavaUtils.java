package com.leelory.suncalc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;

import static java.lang.Math.*;

/**
 * Created with  IDEA.
 *
 * @author mastcer@gmail.com by leelory
 * @description a tiny java library for calculating sun/moon positions and phases
 * 一个用于计算日, 月位置和相位的轻量级的 java工具类
 * <p>
 * Based on a JavaScript library SunCalc for calculating sun/moon position and light phases.
 * https://github.com/mourner/suncalc
 * @date 2019/5/18 : 11:39
 */
public class SunCalc4JavaUtils {

    final static double rad = PI / 180;

    /**
     * sun calculations are based on http://aa.quae.nl/en/reken/zonpositie.html formulas
     * date/time constants and conversions
     */
    final static double dayMs = 1000 * 60 * 60 * 24;
    final static double J1970 = 2440588;
    final static double J2000 = 2451545;

    static double toJulian(Date date) {
        return date.getTime() / dayMs - 0.5 + J1970;
    }

    static Date fromJulian(double j) {
        return new Date((long) ((j + 0.5 - J1970) * dayMs));
    }

    static double toDays(Date date) {
        return toJulian(date) - J2000;
    }

    /**
     * general calculations for position
     * // obliquity of the Earth
     */
    final static double e = rad * 23.4397;

    static double rightAscension(double l, double b) {
        return atan2(sin(l) * cos(e) - tan(b) * sin(e), cos(l));
    }

    static double declination(double l, double b) {
        return asin(sin(b) * cos(e) + cos(b) * sin(e) * sin(l));
    }

    static double azimuth(double H, double phi, double dec) {
        return atan2(sin(H), cos(H) * sin(phi) - tan(dec) * cos(phi));
    }

    static double altitude(double H, double phi, double dec) {
        return asin(sin(phi) * sin(dec) + cos(phi) * cos(dec) * cos(H));
    }

    static double siderealTime(double d, double lw) {
        return rad * (280.16 + 360.9856235 * d) - lw;
    }

    static double astroRefraction(double h) {
        // the following formula works for positive altitudes only.
        if (h < 0) {
            // if h = -0.08901179 a div/0 would occur.
            h = 0;
        }
        // formula 16.4 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        // 1.02 / tan(h + 10.26 / (h + 5.10)) h in degrees, result in arc minutes -> converted to rad:
        return 0.0002967 / tan(h + 0.00312536 / (h + 0.08901179));
    }

    /**
     * general sun calculations
     *
     * @param d
     * @return
     */
    static double solarMeanAnomaly(double d) {
        return rad * (357.5291 + 0.98560028 * d);
    }

    static double eclipticLongitude(double M) {
        // equation of center
        double C = rad * (1.9148 * sin(M) + 0.02 * sin(2 * M) + 0.0003 * sin(3 * M));
        // perihelion of the Earth
        double P = rad * 102.9372;
        return M + C + P + PI;
    }

    static Map<String, Double> sunCoords(double d) {
        Map map = Maps.newConcurrentMap();
        double M = solarMeanAnomaly(d);
        double L = eclipticLongitude(M);
        map.put("dec", declination(L, 0));
        map.put("ra", rightAscension(L, 0));
        return map;
    }


    /**
     * calculates sun position for a given date and latitude/longitude
     * 计算给定日期和纬度/经度的太阳位置
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */

    public static Map<String, Double> getPosition(Date date, double lat, double lng) {
        Map map = Maps.newConcurrentMap();
        double lw = rad * -lng;
        double phi = rad * lat;
        double d = toDays(date);
        Map<String, Double> c = sunCoords(d);
        double H = siderealTime(d, lw) - c.get("ra");
        map.put("azimuth", azimuth(H, phi, c.get("dec")));
        map.put("altitude", altitude(H, phi, c.get("dec")));
        return map;
    }

    ;


    /**
     * * sun times configuration (angle, morning name, evening name)
     * sunrise	日出（太阳的顶部边缘出现在地平线上）
     * sunriseEnd	日出结束（太阳的底部边缘接触地平线）
     * goldenHourEnd	早上黄金时段（柔和的光线，摄影的最佳时间）结束
     * solarNoon	太阳正午（太阳位于最高位置）
     * goldenHour	晚上黄金时段开始
     * sunsetStart	日落开始（太阳的底部边缘接触地平线）
     * sunset	日落（太阳消失在地平线以下，晚上民间黄昏开始）
     * dusk	黄昏（傍晚航海黄昏开始）
     * nauticalDusk	航海黄昏（晚上天文学黄昏开始）
     * night	夜晚开始（黑暗足以进行天文观测）
     * nadir	最低点（夜晚最黑暗的时刻，太阳处于最低位置）
     * nightEnd	夜晚结束（早晨天文学黄昏开始）
     * nauticalDawn	航海黎明（早上航海暮光之城开始）
     * dawn	黎明（早晨航海黄昏结束，早晨民间黄昏开始）
     */
    static List times = Lists.newArrayList(
            Lists.newArrayList(-0.833D, "sunrise", "sunset"),
            Lists.newArrayList(-0.3D, "sunriseEnd", "sunsetStart"),
            Lists.newArrayList(-6D, "dawn", "dusk"),
            Lists.newArrayList(-12D, "nauticalDawn", "nauticalDusk"),
            Lists.newArrayList(-18D, "nightEnd", "night"),
            Lists.newArrayList(6D, "goldenHourEnd", "goldenHour"));

    /**
     * adds a custom time to the times config
     *
     * @param angle
     * @param riseName
     * @param setName
     */
    public static void addTime(double angle, String riseName, String setName) {
        times.add(Lists.newArrayList(angle, riseName, setName));
    }


    /**
     * calculations for sun times
     */

    final static double J0 = 0.0009;

    static double julianCycle(double d, double lw) {
        return round(d - J0 - lw / (2 * PI));
    }

    static double approxTransit(double Ht, double lw, double n) {
        return J0 + (Ht + lw) / (2 * PI) + n;
    }

    static double solarTransitJ(double ds, double M, double L) {
        return J2000 + ds + 0.0053 * sin(M) - 0.0069 * sin(2 * L);
    }

    static double hourAngle(double h, double phi, double d) {
        return acos((sin(h) - sin(phi) * sin(d)) / (cos(phi) * cos(d)));
    }

    /**
     * returns set time for the given sun altitude
     *
     * @param h
     * @param lw
     * @param phi
     * @param dec
     * @param n
     * @param M
     * @param L
     * @return
     */
    static double getSetJ(double h, double lw, double phi, double dec, double n, double M, double L) {
        double w = hourAngle(h, phi, dec),
                a = approxTransit(w, lw, n);
        return solarTransitJ(a, M, L);
    }


    /**
     * calculates sun times for a given date and latitude/longitude
     * 计算给定日期和纬度/经度的太阳时间
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */

    public static Map<String, Date> getTimes(Date date, double lat, double lng) {

        double lw = rad * -lng;
        double phi = rad * lat;

        double d = toDays(date);
        double n = julianCycle(d, lw);
        double ds = approxTransit(0, lw, n);

        double M = solarMeanAnomaly(ds);
        double L = eclipticLongitude(M);
        double dec = declination(L, 0);

        double jNoon = solarTransitJ(ds, M, L);

        Map result = Maps.newConcurrentMap();
        result.put("solarNoon", fromJulian(jNoon));
        result.put("nadir", fromJulian(jNoon - 0.5));

        for (int i = 0, len = times.size(); i < len; i += 1) {
            List time = (List) times.get(i);
            double jSet = getSetJ(Double.valueOf(time.get(0).toString()) * rad, lw, phi, dec, n, M, L);
            double jRise = jNoon - (jSet - jNoon);

            result.put(time.get(1), fromJulian(jRise));
            result.put(time.get(2), fromJulian(jSet));
        }

        return result;
    }

    ;


    /**
     * moon calculations, based on http://aa.quae.nl/en/reken/hemelpositie.html formulas
     *
     * @param d
     * @return
     */
    static Map<String, Double> moonCoords(double d) {
        // geocentric ecliptic coordinates of the moon
        Map result = Maps.newConcurrentMap();
        // ecliptic longitude
        double L = rad * (218.316 + 13.176396 * d);
        // mean anomaly
        double M = rad * (134.963 + 13.064993 * d);
        //// mean distance
        double F = rad * (93.272 + 13.229350 * d);
        // longitude
        double l = L + rad * 6.289 * sin(M);
        // latitude
        double b = rad * 5.128 * sin(F);
        // distance to the moon in km
        double dt = 385001 - 20905 * cos(M);
        result.put("ra", rightAscension(l, b));
        result.put("dec", declination(l, b));
        result.put("dist", dt);
        return result;
    }

    /**
     * calculates moon position for a given date and latitude/longitude
     * 计算给定日期和纬度/经度的月亮位置
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */
    public static Map<String, Double> getMoonPosition(Date date, double lat, double lng) {
        Map result = Maps.newConcurrentMap();
        double lw = rad * -lng;
        double phi = rad * lat;
        double d = toDays(date);

        Map<String, Double> c = moonCoords(d);
        double H = siderealTime(d, lw) - c.get("ra");
        double h = altitude(H, phi, c.get("dec"));
        // formula 14.1 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
        double pa = atan2(sin(H), tan(phi) * cos((double) c.get("dec")) - sin((double) c.get("dec")) * cos(H));
        // altitude correction for refraction
        h = h + astroRefraction(h);
        result.put("azimuth", azimuth(H, phi, (double) c.get("dec")));
        result.put("altitude", h);
        result.put("distance", c.get("dist"));
        result.put("parallacticAngle", pa);
        return result;
    }

    ;


    /**
     * calculations for illumination parameters of the moon
     * 月球照明参数的计算
     * // based on http://idlastro.gsfc.nasa.gov/ftp/pro/astro/mphase.pro formulas and
     * // Chapter 48 of "Astronomical Algorithms" 2nd edition by Jean Meeus (Willmann-Bell, Richmond) 1998.
     *
     * @param date
     * @return
     */
    public static Map<String, Double> getMoonIllumination(Date date) {
        Map result = Maps.newConcurrentMap();
        double d = toDays(date == null ? new Date() : date);
        Map<String, Double> s = sunCoords(d);
        Map<String, Double> m = moonCoords(d);
        // distance from Earth to Sun in km
        double sdist = 149598000;

        double phi = acos(sin(s.get("dec")) * sin(m.get("dec")) + cos(s.get("dec")) * cos(m.get("dec")) * cos(s.get("ra") - m.get("ra")));
        double inc = atan2(sdist * sin(phi), m.get("dist") - sdist * cos(phi));
        double angle = atan2(cos(s.get("dec")) * sin(s.get("ra") - m.get("ra")), sin(s.get("dec")) * cos(m.get("dec")) - cos(s.get("dec")) * sin(m.get("dec")) * cos(s.get("ra") - m.get("ra")));
        result.put("fraction", (1 + cos(inc)) / 2);
        result.put("phase", 0.5 + 0.5 * inc * (angle < 0 ? -1 : 1) / PI);
        result.put("angle", angle);
        return result;
    }

    ;


    static Date hoursLater(Date date, double h) {
        return new Date((long) (date.getTime() + h * dayMs / 24));
    }

    /**
     * calculations for moon rise/set times
     * 月亮升起/落下时间的计算
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */
    public static Map<String, Date> getMoonTimes(Date date, double lat, double lng) {
        return getMoonTimes(date, lat, lng, false);
    }

    /**
     * calculations for moon rise/set times are based on http://www.stargazing.net/kepler/moonrise.html article
     * 月亮升起/落下时间的计算
     *
     * @param date
     * @param lat
     * @param lng
     * @return
     */
    public static Map<String, Date> getMoonTimes(Date date, double lat, double lng, boolean isUTC) {
        Map result = Maps.newConcurrentMap();
        //is GMT
        Calendar calendar = Calendar.getInstance();
        if (isUTC) {
            calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        }
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date t = calendar.getTime();

        double hc = 0.133 * rad;
        double h0 = getMoonPosition(t, lat, lng).get("altitude") - hc;
        double h1, h2, rise = 0, set = 0, a, b, xe, ye = 0, d, roots, x1 = 0, x2 = 0, dx;

        // go in 2-hour chunks, each time seeing if a 3-point quadratic curve crosses zero (which means rise or set)
        for (int i = 1; i <= 24; i += 2) {
            h1 = getMoonPosition(hoursLater(t, i), lat, lng).get("altitude") - hc;
            h2 = getMoonPosition(hoursLater(t, i + 1), lat, lng).get("altitude") - hc;

            a = (h0 + h2) / 2 - h1;
            b = (h2 - h0) / 2;
            xe = -b / (2 * a);
            ye = (a * xe + b) * xe + h1;
            d = b * b - 4 * a * h1;
            roots = 0;

            if (d >= 0) {
                dx = Math.sqrt(d) / (Math.abs(a) * 2);
                x1 = xe - dx;
                x2 = xe + dx;
                if (Math.abs(x1) <= 1) {
                    roots++;
                }
                if (Math.abs(x2) <= 1) {
                    roots++;
                }
                if (x1 < -1) {
                    x1 = x2;
                }
            }

            if (roots == 1) {
                if (h0 < 0) {
                    rise = i + x1;
                } else {
                    set = i + x1;
                }

            } else if (roots == 2) {
                rise = i + (ye < 0 ? x2 : x1);
                set = i + (ye < 0 ? x1 : x2);
            }

            if (rise != 0 && set != 0) {
                break;
            }

            h0 = h2;
        }

        if (rise != 0) {
            result.put("rise", hoursLater(t, rise));
        }
        if (set != 0) {
            result.put("set", hoursLater(t, set));
        }
        if (rise == 0 && set == 0) {
            result.put(ye > 0 ? "alwaysUp" : "alwaysDown", true);
        }
        return result;
    }

    ;
}
