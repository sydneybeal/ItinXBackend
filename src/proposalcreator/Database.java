/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proposalcreator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
/**
 *
 * @author Baron
 */
public class Database {
    
    Database(){
        
    }
    
   public static List<Property> loadProperties() throws FileNotFoundException, IOException{
        InputStream ExcelFileToRead = new FileInputStream("./PropertyDatabase.xls");
        HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);

        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;

        //an iterator to work through all the rows in the sheet
        Iterator rows = sheet.rowIterator();
        
        //initialize a new empty ArrayList of properties (allows for dynamic list size)
        List<Property> PropertyList = new ArrayList<Property>();

        //initialize an empty property to use for cycling through the sheet
        Property myProperty = new Property();
        int columnIndex;
        //run this loop for every row in the sheet
        while (rows.hasNext()) {
            
            row = (HSSFRow) rows.next();
            Iterator cells = row.cellIterator();
            //run this loop for every cell in the sheet
            while (cells.hasNext()) {
                cell = (HSSFCell) cells.next();
                columnIndex = cell.getColumnIndex();
                switch (columnIndex) {
                    case 0:
                        myProperty.setName(cell.getStringCellValue());
                        break;
                    case 1:
                        myProperty.setCity(cell.getStringCellValue());
                        break;
                    case 2:
                        myProperty.setCountry(cell.getStringCellValue());
                        break;
                    case 3:
                        myProperty.setInclusions(cell.getStringCellValue());
                        break;
                    case 4:
                        myProperty.setNotes(cell.getStringCellValue());
                        break;
                    case 5:
                        myProperty.setType(cell.getStringCellValue().toLowerCase());
                        break;
                    case 6:
                        myProperty.setWebpage(cell.getStringCellValue());
                        break;
                    case 7:
                        myProperty.setAirstrip(cell.getStringCellValue());
                        break;
                    default:
                        break;
                }
            }
            //add the new property to the ArrayList
            PropertyList.add(myProperty);
            //reset the property for the next Iteration
            myProperty = new Property();
        }
        wb.close();
        ExcelFileToRead.close();
        System.out.println("Completed");

        return (PropertyList);
    }
    
   public void addNewProperty(Property newProperty)throws FileNotFoundException, IOException{
       //Code to add new properties to the database
        InputStream ExcelFileToRead = new FileInputStream("PropertyDatabase.xls");
        
        HSSFWorkbook wb = new HSSFWorkbook(ExcelFileToRead);
        
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row;
        HSSFCell cell;
        
        int newrownum = sheet.getLastRowNum() + 1;
        row = sheet.createRow(newrownum);
        System.out.println("Created Row at " + newrownum);
        System.out.println(sheet.getLastRowNum());
        
        cell = row.createCell(0);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getName());
        
        cell = row.createCell(1);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getCity());
        
        cell = row.createCell(2);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getCountry());
        
        cell = row.createCell(3);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getInclusions());
        
        cell = row.createCell(4);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getNotes());
        
        cell = row.createCell(5);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getType());
        
        cell = row.createCell(6);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getWebpage());
        
        cell = row.createCell(7);
        cell.setCellType(CellType.STRING);
        cell.setCellValue(newProperty.getAirstrip());
//        OutputStream ExcelFileToWrite = new FileOutputStream("PropertyDatabase.xls");
//        wb.write(ExcelFileToWrite);
//        ExcelFileToWrite.flush();
//        ExcelFileToWrite.close();

        OutputStream ExcelFileToWrite = new FileOutputStream("PropertyDatabase.xls");
        wb.write(ExcelFileToWrite);
        ExcelFileToWrite.flush();
        ExcelFileToWrite.close();

        
   }
    
    
}
