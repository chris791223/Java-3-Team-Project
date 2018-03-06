/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IPD12.ProjectManagement;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.constant.TimePeriod;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
public class ProjectTasksTimeSeriesReport {
    private String Title = "";
    public ProjectTasksTimeSeriesReport(String title) {
        this.Title = title;
        build();
    }
    private void build() {
		FontBuilder boldFont = stl.fontArialBold().setFontSize(12);

		//TextColumnBuilder<Date> orderDateColumn = col.column("Date", "Date", type.dateYearToMonthType());
                TextColumnBuilder<Date> orderDateColumn = col.column("Date", "Date", type.dateType());
		TextColumnBuilder<Integer> quantityColumn = col.column("AllTasks", "AllTasks", type.integerType());
		TextColumnBuilder<Integer> priceColumn = col.column("LeftTasks", "LeftTasks", type.integerType());

		try {
			report()
					.setTemplate(Templates.reportTemplate)
					.columns(orderDateColumn, quantityColumn, priceColumn)
					.title(Templates.createTitleComponent(Title))
					.summary(
							cht.timeSeriesChart()
									.setTitle(Title)
									.setTitleFont(boldFont)
									.setTimePeriod(orderDateColumn)
									.setTimePeriodType(TimePeriod.DAY)
									.series(
											cht.serie(quantityColumn), cht.serie(priceColumn))
									.setTimeAxisFormat(
											cht.axisFormat().setLabel("Date")))
					.pageFooter(Templates.footerComponent)
					.setDataSource(createDataSource())
					.show(false);
		} catch (DRException e) {
			e.printStackTrace();
		}
	}

	private JRDataSource createDataSource() {
		DRDataSource dataSource = new DRDataSource("Date", "AllTasks", "LeftTasks");
                
                Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -15);
		for (int i = 0; i < 15; i++) {
			dataSource.add(c.getTime(),(int)(Math.random()*50)+50,(int)(Math.random()*50));
			c.add(Calendar.DAY_OF_MONTH, 1);
		}		
		return dataSource;
	} 
}
