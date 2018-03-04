/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author wjing
 */
public class GlobalProcess {

    static String DATE_PATTERN = "YYYY-MM-DD";
    static String BLANK = "";

    public static String formatOutputDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return (date != null) ? sdf.format(date) : DATE_PATTERN;
    }
    
    public static String formatOutputDate2(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return (date != null) ? sdf.format(date) : BLANK;
    }

    public static java.sql.Date formatSqlDate(Date date) {
        return (date != null) ? new java.sql.Date(date.getTime()) : null;
    }

    public static Date checkDateFormat(String dateStr) {
        Date date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            date = sdf.parse(dateStr);
        }
        catch (ParseException ex) {
            date = null;
        }
        return date;
    }

}
