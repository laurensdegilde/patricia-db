package generator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class Generator {
    
    private final String XLS_FILE_PATH = getClass().getClassLoader().getResource("generation-codes.xlsx").getFile();
    private Map<String, String> generatedDataRepresentation;
    
    private Workbook workbook;
    private Sheet sheet;
    
    
    public Generator() throws IOException, InvalidFormatException {
        this.generatedDataRepresentation = new HashMap<>();
        this.readPredefinedCodes();
    }
    
    public List<String[]> generateRecordForUser(int userId, int amountOfAttributes) {
        int amountOfValues = 400;
        Random random = new Random();
        String key;
        String value;
        
        List<String[]> generatedListOfUserData = new ArrayList<>();
        boolean wroteSpecificYet = false;
        boolean isSpecific = false;
        
        for (int i = 0; i < amountOfAttributes; i++) {
            if (wroteSpecificYet != true) {
                isSpecific = random.nextBoolean();
            }
            key = this.sheet.getRow(userId).getCell(0).getStringCellValue() + this.sheet.getRow(random.nextInt(amountOfValues)).getCell(1).getStringCellValue();
            value = String.valueOf(sheet.getRow(random.nextInt(amountOfValues)).getCell(2).getNumericCellValue());
            
            if (isSpecific) {
                String[] kv = getSpecificRecord(userId);
                generatedListOfUserData.add(kv);
                this.generatedDataRepresentation.put(kv[0], kv[1]);
                wroteSpecificYet = true;
                isSpecific = false;
            }else {
                generatedListOfUserData.add(new String[]{key, value});
                this.generatedDataRepresentation.put(key, value);
            }
            
        }
        if (!wroteSpecificYet) {
            generatedListOfUserData.remove(generatedListOfUserData.size() - 1);
            generatedListOfUserData.add(getSpecificRecord(userId));
        }
        return generatedListOfUserData;
    }
    private String[] getSpecificRecord(int userId){
        String key = this.sheet.getRow(userId).getCell(0).getStringCellValue() + "_" + "eigen_risico";
        String value = "385";
        return new String[]{key, value};
    }
    private void readPredefinedCodes() throws IOException, InvalidFormatException {
        this.workbook = WorkbookFactory.create(new File(XLS_FILE_PATH));
        this.sheet = workbook.getSheetAt(0);
    }
    
    public Map<String, String> getGeneratedDataRepresentation() {
        return generatedDataRepresentation;
    }
}
