
import com.leelory.suncalc.SunCalc4JavaUtils;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

/**
 * Created with  IDEA.
 *
 * @author mastcer@gmail.com by leelory
 * @description test  SunCalc4JavaUtils
 */
public class SimpleTest {

    @Test
    public void testSunCalc4JavaUtils() {
        Map<String, Date> map1 = SunCalc4JavaUtils.getTimes(new Date(), 31.87, 117.24);

        Map<String, Double> map2 = SunCalc4JavaUtils.getPosition(new Date(), 31.87, 117.24);

        Map<String, Date> map3 = SunCalc4JavaUtils.getMoonTimes(new Date(), 31.87, 117.24);

        Map<String, Double> map4 = SunCalc4JavaUtils.getMoonPosition(new Date(), 31.87, 117.24);

        Map<String, Double> map5 = SunCalc4JavaUtils.getMoonIllumination(new Date());
    }
}
