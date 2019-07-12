package ai.turbochain.ipex;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import jxl.write.DateTime;

public class FinanceTest  {
	
	public static long utcToTimestamp(DateTime dataTime) throws ParseException {
    	SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	df2.setTimeZone(TimeZone.getTimeZone("UTC"));
    	Date date = df2.parse(dataTime.toString());
    	return date.getTime();
   }
	
	private static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") ;
	
	/**
	 * 得到UTC时间，类型为字符串，格式为"yyyy-MM-dd HH:mm:ss"
	 * 如果获取失败，返回null
	 * @return
	 */
	public static String getUTCTimeStr() {
		StringBuffer UTCTimeBuffer = new StringBuffer();
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance() ;
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE); 
		int second = cal.get(Calendar.SECOND);
		   
		UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day) ;
		UTCTimeBuffer.append(" ").append(hour).append(":").append(minute).append(":").append(second) ;
		try{
			format.parse(UTCTimeBuffer.toString()) ;
			return UTCTimeBuffer.toString() ;
		}catch(ParseException e)
		{
			e.printStackTrace() ;
		}
		return null ;
	}
	
	
	@Test
    public void ee(){
		 Calendar calendar = Calendar.getInstance();
	     calendar.add(Calendar.HOUR_OF_DAY, 24 * 7);
	     System.out.print(format.format(calendar.getTime()));
	}
    //@Test
    public void testBigdecimal(){
        BigDecimal a  = new BigDecimal("12.5");
        System.out.println(a);
        a.subtract(BigDecimal.ONE);
        System.out.println(a);
        
        
        	 
    }
}



