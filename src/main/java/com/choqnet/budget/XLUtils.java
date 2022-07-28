package com.choqnet.budget;

import com.choqnet.budget.entity.*;
import com.choqnet.budget.entity.datalists.ProjectType;
import com.choqnet.budget.entity.datalists.Source;
import io.jmix.core.DataManager;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.SchemaOutputResolver;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class XLUtils {
    private static final Logger log = LoggerFactory.getLogger(XLUtils.class);
    private XSSFSheet np, ogp, run;
    private CellStyle cellStyle;
    private XSSFWorkbook wb;
    private List<IPRB> iprbs;
    private List<Mapping> mappingList;
    private Budget currentBudget;
    private List<Detail> details;
    private FormulaEvaluator evaluator;
    private List<ErrorMessage> errorLog = new ArrayList<>();
    private int iNP, iOGP, iRUN;
    XSSFFont font;
    private final List<XSSFSheet> sheets = new ArrayList<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yy");

    @Autowired
    private DataManager dataManager;

    // *** initialisation: gets and processes the data export to MSBudget Acceptance file.
    public XSSFWorkbook processFile(XSSFWorkbook workbook, Budget budget) {
        wb = workbook;
        font = wb.createFont();
        font.setFontHeight(8.0);
        font.setFontName("Courier New");
        evaluator = wb.getCreationHelper().createFormulaEvaluator();
        currentBudget = budget;
        getContext();
        manageProjects();
        setAllRowSum();
        writeEffortSums();
        return wb;
    }

    public List<ErrorMessage> giveErrorLog() {
        return errorLog;
    }

    // debug *** test writing in Excel file
    private static <K, V> V getValue(Map<K, V> map, K key) {

        return map.entrySet().stream()
                .filter(e -> key.equals(e.getKey()))
                .findFirst().map(Map.Entry::getValue)
                .orElse(null);
    }

    public XSSFWorkbook processFileDebug(XSSFWorkbook workbook, Budget budget) {
        // clean the existing error messages
        errorLog = new ArrayList<>();

        wb = workbook;
        // processing : write few values in the template
        List<IPRB> expected, selection;

        List<Detail> details, range;
        List<String> feedback = new ArrayList<>();
        int nbRows;
        final int START_ROW = 15;
        final int START_BUDGET = 594; // col VW

        details = dataManager.load(Detail.class).query("select e from Detail e where e.included = TRUE AND e.budget = :budget").parameter("budget", budget).fetchPlan("details").list();

        XSSFSheet sheet = wb.getSheet("MS Roadmap");
        // step 1 : get the list of IPRB supplied in the Current Plan
        Map<String, Integer> supplied = new HashMap<>();
        String temp;
        int counter=START_ROW;
        do {
            temp = getXCell(counter,2, sheet).getStringCellValue();
            if (!temp.equals("")) {supplied.put(temp, counter);}
            counter+=1;
        } while (!temp.equals(""));
        nbRows = counter-1;

        // step 2 : get the list of the expected IPRB: having demand, actual or xlactual
        expected = dataManager.load(Detail.class).query("select e from Detail e where e.budget = :budget").parameter("budget", budget).list()
                .stream().filter(e -> e.getIncluded()).map(Detail::getIprb).distinct().collect(Collectors.toList());
        // this part allows including projects having actuals for the year of the budget
        // while building the budget 2023, no projects will have 2023's actuals
        /*
        List<IPRB> started = dataManager.load(Actual.class).query("select e from Actual e where e.finMonth like 'fin" +  budget.getYear() + "%'").list()
                .stream().map(e -> e.getIprb()).distinct().collect(Collectors.toList());
        expected = Stream.concat(expected.stream(), started.stream()).distinct().collect(Collectors.toList());
        started = dataManager.load(XLActual.class).query("select e from XLActual e where e.year = :year").parameter("year", budget.getYear()).list()
                .stream().map(e -> e.getIprb()).distinct().collect(Collectors.toList());
        expected = Stream.concat(expected.stream(), started.stream()).distinct().collect(Collectors.toList());

        */
        expected.remove(null);
        expected = expected.stream().filter(e -> !e.getOutBudget()).collect(Collectors.toList());

        // step 3 : check missing IPRB in the host Excel file
        for (IPRB iprb: expected) {
            if (getValue(supplied, iprb.getReference())==null) {
                ErrorMessage errorMessage = dataManager.create(ErrorMessage.class);
                errorMessage.setItemID(iprb.getReference());
                errorMessage.setErrorMessage("Missing: " + iprb.getName());
                errorLog.add(errorMessage);
                // feedback.add(iprb.getReference() + " is missing in the Excel file");
            }
        }

        // step 4 : copy of metadata
        for (int i = START_ROW; i<nbRows; i++) {
            String iprb_reference = getXCell(i,2,sheet).getStringCellValue();
            if (iprb_reference!=null) {
                IPRB target = expected.stream().filter(e -> iprb_reference.equals(e.getReference())).findFirst().orElse(null);
                if (target!=null) {
                    cellText(i, 3, target.getName(), sheet);
                    cellText(i, 4, target.getActivityType()==null ? "???" : target.getActivityType().getId(), sheet);
                    cellText(i, 5, target.getCategory()==null ? "" : target.getCategory().getId(), sheet);
                    cellText(i, 6, target.getStrategyCategory()==null ? "" : target.getStrategyCategory().getId(), sheet);
                    cellText(i, 7, target.getRevenueCategory()==null ? "" : target.getRevenueCategory().getId(), sheet);
                    cellText(i, 8, target.getLegalEntity()==null ? "" : target.getLegalEntity().getId(), sheet);
                    cellText(i, 12, target.getProgram()==null ? "" : target.getProgram().getId(), sheet);

                    cellText(i, 13, target.getOwner(), sheet);
                    cellText(i,16, target.getEstCAPI()==null ? "" : target.getEstCAPI().getId(), sheet);
                    cellText(i,17, target.getEstOOI()==null ? "" : target.getEstOOI().getId(), sheet);
                }
            }
        }

        // step 5 - get the Platform names
        counter = START_BUDGET;
        boolean finished = false;
        String readValue;
        do {
            readValue = getXCell(0, counter, sheet).getStringCellValue();
            finished = "END".equals(readValue.toUpperCase());
            if (!"".equals(readValue)) {

                // get the involved IPRBs
                String finalReadValue = readValue;
                selection = details.stream().filter(e -> finalReadValue.equals(e.getTeam().getIcTarget())).map(Detail::getIprb).distinct().collect(Collectors.toList());
                for (IPRB iprb: selection) {

                    if (supplied.containsKey(iprb.getReference())) {
                        int row = getValue(supplied, iprb.getReference());
                        range = details.stream().filter(e -> iprb.equals(e.getIprb()) && finalReadValue.equals(e.getTeam().getIcTarget())).collect(Collectors.toList());

                        cellNum(row,counter+1, range.stream().map(Detail::getMdQ1).reduce(0.0, Double::sum), sheet);
                        cellNum(row,counter+2, range.stream().map(Detail::getMdQ2).reduce(0.0, Double::sum), sheet);
                        cellNum(row,counter+3, range.stream().map(Detail::getMdQ3).reduce(0.0, Double::sum), sheet);
                        cellNum(row,counter+4, range.stream().map(Detail::getMdQ4).reduce(0.0, Double::sum), sheet);
                    }
                }
            }
            counter ++;
        } while(counter<5000 && !finished );



        xriteNUM(15,602, 12.0,sheet);
        xriteNUM(15,603, 37.5,sheet);
        xriteNUM(1,1,11.0, sheet);
        wb.setForceFormulaRecalculation(true);
        return wb;
    }

    private void cellText(int iRow, int iCol, String value, XSSFSheet sheet) {
        sheet.getRow(iRow);
        Cell cell = getXCell(iRow, iCol, sheet);
        cell.setCellValue(value);
    }

    private void cellNum(int iRow, int iCol, double value, XSSFSheet sheet) {
        sheet.getRow(iRow);
        Cell cell = getXCell(iRow, iCol, sheet);
        cell.setCellValue(value);
    }

    private void xriteNUM(int iRow, int iCol, Double value, XSSFSheet sheet) {
        sheet.getRow(iRow);
        Cell cell = getXCell(iRow, iCol, sheet);
        CellStyle style = wb.createCellStyle();
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("0"));
        cell.setCellValue(value);
//        cell.setCellStyle(style);
//        cell.removeFormula();
//        cellStyle = wb.createCellStyle();
//        cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#"));
//        cellStyle.setFont(font);
//        cell.setCellValue(value);
//        cell.setCellType(CellType.NUMERIC);
        //cell.setCellStyle(cellStyle);
        //evaluator.evaluate(cell);
    }

    // end debug

    private void getContext() {
        iprbs = dataManager.load(IPRB.class)
                .query("select e from IPRB e where e.outBudget = FALSE").list();
        mappingList = dataManager.load(Mapping.class)
                .query("select e from Mapping e where e.isMD = true")
                .list();
        details = dataManager.load(Detail.class)
                .query("select e from Detail e where e.demand.budget = :budget and e.priority <= :priority")
                .parameter("budget", currentBudget)
                .parameter("priority", currentBudget.getPrioThreshold().getId())
                .list();

        np = wb.getSheet("New Projects");
        ogp = wb.getSheet("Ongoing Projects");
        run = wb.getSheet("Non-Projects (RUN-BAU)");
        sheets.add(np);
        sheets.add(ogp);
        sheets.add(run);
        iNP = 6;
        iOGP = 6;
        iRUN = 6;
    }

    // *** Data processing functions
    private void manageProjects() {
        for (IPRB iprb : iprbs) {
            // writes the metadata columns
            if (iprb.getProjectType() == null) {
                // log the error - temp way
                log.info(iprb.getReference() + " doesn't get a valid type");
                writeAllMetaData(iNP, iprb, np);
                writeEfforts(iNP, iprb, np);
                iNP++;
            } else {
                switch (iprb.getProjectType().getId()) {
                    case "New Project":
                        writeAllMetaData(iNP, iprb, np);
                        writeEfforts(iNP, iprb, np);
                        iNP++;
                        break;
                    case "Ongoing Project":
                        writeAllMetaData(iOGP, iprb, ogp);
                        writeEfforts(iOGP, iprb, ogp);
                        iOGP++;
                        break;
                    default:
                        writeAllMetaData(iRUN, iprb, run);
                        writeEfforts(iRUN, iprb, run);
                        iRUN++;
                        break;
                }
            }
        }
    }


    // *** Utility Functions
    public String readCell(Cell c) {
        if (c == null) {
            return "";
        }
        switch (c.getCellType()) {
            case NUMERIC:
                return String.valueOf(c.getNumericCellValue());
            case STRING:
                return c.getStringCellValue();
            case FORMULA:
                switch (c.getCachedFormulaResultType().toString()) {
                    case "STRING":
                        return c.getStringCellValue();
                    case "NUMERIC":
                        return new DecimalFormat("#.0#").format(c.getNumericCellValue());
                    default:
                        log.info(">>> UploadIPRB (warning message) unknown type: " + c.getCachedFormulaResultType().toString());
                        return c.getCachedFormulaResultType().toString();
                }
            case BOOLEAN:
                return (c.getBooleanCellValue() ? "yes" : "no");
            case ERROR:
                return "Err";
            case _NONE:
            case BLANK:
                return "";
            default:
                return "???";
        }
    }

    private double getDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return 0.00;
        }
    }

    private void writeAllMetaData(int iRow, IPRB iprb, XSSFSheet sheet) {
        writeMetaData(iRow, 2, iprb.getReference().replace(" ",""), sheet);
        writeMetaData(iRow, 3, iprb.getName(), sheet);
        writeMetaData(iRow, 4, iprb.getPortfolioClassification() == null ? "" : iprb.getPortfolioClassification().getId(), sheet);
        writeMetaData(iRow, 5, iprb.getLegalEntity() == null ? "" : iprb.getLegalEntity().getId(), sheet);
        writeMetaData(iRow, 6, iprb.getStrategicProgram() == null ? "" : iprb.getStrategicProgram().getId(), sheet);
        writeMetaData(iRow, 7, iprb.getActivityType() == null ? "" : iprb.getActivityType().getId(), sheet);
        writeMetaData(iRow, 8, iprb.getNewProductIndicator() == null ? "" : iprb.getNewProductIndicator().name(), sheet);
        writeMetaData(iRow, 9, iprb.getGroupOffering() == null ? "" : iprb.getGroupOffering().getId(), sheet);
        writeMetaData(iRow, 10, iprb.getOwner(), sheet);
        writeMetaData(iRow, 11, iprb.getEstCAPI() == null ? "" : iprb.getEstCAPI().getId(), sheet);
        writeMetaData(iRow, 12, iprb.getEstOOI() == null ? "" : iprb.getEstOOI().getId(), sheet);
        writeMetaData(iRow, 13, iprb.getStartDate() == null ? "" : iprb.getStartDate().format(dtf), sheet);
        writeMetaData(iRow, 14, iprb.getEndDate() == null ? "" : iprb.getEndDate().format(dtf), sheet);
    }

    private void writeEfforts(int iRow, IPRB iprb, XSSFSheet sheet) {
        int destCol;
        String destName;
        for (Mapping mapping : mappingList) {
            destCol = offset(mapping.getColNP(), sheet);
            String finalDestName = mapping.getCode();
            double effort = details.stream().filter(e ->
                    e.getTeam() != null &&
                            finalDestName.equals(e.getTeam().getIcTarget()) &&
                            iprb.equals(e.getProgress().getIprb())
            ).map(Detail::getMdY).reduce(Double::sum).orElse(0.0);
            if (!(Double.compare(effort, 0.0) == 0)) {
                writeNUM(iRow, destCol, effort, sheet);
            }
        }
    }

    private void writeEffortSums() {
        int destCol;
        ProjectType projType;
        for (XSSFSheet sheet : sheets) {
            projType = giveProjectType(sheet);
            for (Mapping mapping : mappingList) {
                destCol = offset(mapping.getColNP(), sheet);
                String finalDestName = mapping.getCode();
                ProjectType finalProjType = projType;
                double effort = details.stream().filter(e ->
                        e.getTeam() != null &&
                                finalDestName.equals(e.getTeam().getIcTarget()) &&
                                finalProjType.equals(e.getProgress().getIprb().getProjectType())
                ).map(Detail::getMdY).reduce(Double::sum).orElse(0.0);
                if (!(Double.compare(effort, 0.0) == 0)) {
                    writeNUM(4, destCol, effort, sheet);
                    Cell c = getXCell(1,destCol,sheet);
                    String val =  readCell(c);
                    double rate = getDouble(readCell(getXCell(1,destCol,sheet)).replace(",","."));
                    writeNUM(3, destCol, effort*rate, sheet);
                }
            }
        }
    }

    private int offset(int i, XSSFSheet sheet) {
        if (np.equals(sheet)) {
            return i;
        } else if (ogp.equals(sheet)) {
            return i - 1;
        }
        return i - 12;
    }

    private Cell getXCell(int iRow, int iCol, XSSFSheet sheet) {
        // root reference is (0, 0)
        XSSFRow row = sheet.getRow(iRow);
        if (row == null) {
            row = sheet.createRow(iRow);
        }
        Cell cell = row.getCell(iCol);
        if (cell == null) {
            cell = row.createCell(iCol);
        }
        return cell;
    }

    private void writeMetaData(int iRow, int iColNP, String value, XSSFSheet sheet) {
        if (sheet.equals(ogp) || sheet.equals(run)) {
            iColNP = iColNP - 1;
        }
        if (!(sheet.equals(run) && iColNP > 2)) {
            writeTEXT(iRow, iColNP, value, sheet);
        }
    }

    private void writeTEXT(int iRow, int iCol, String value, XSSFSheet sheet) {
        Cell cell = getXCell(iRow, iCol, sheet);
        cell.removeFormula();
        cell.setCellValue(value);
        cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        cell.setCellStyle(cellStyle);
        evaluator.evaluate(cell);
    }

    private void writeNUM(int iRow, int iCol, Double value, XSSFSheet sheet) {
        Cell cell = getXCell(iRow, iCol, sheet);
        cell.removeFormula();
        cellStyle = wb.createCellStyle();
        //cellStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#"));
        //cellStyle.setFont(font);
        cell.setCellValue(value);
        //cell.setCellType(CellType.NUMERIC);
        //cell.setCellStyle(cellStyle);
        evaluator.evaluate(cell);
    }

    private void writeDATE(int iRow, int iCol, Date value, XSSFSheet sheet) {
        Cell cell = getXCell(iRow, iCol, sheet);
        cell.removeFormula();
        cellStyle = wb.createCellStyle();
        cellStyle.setDataFormat(
                wb.getCreationHelper().createDataFormat().getFormat("dd/MM/YY")
        );
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
        evaluator.evaluate(cell);
    }

    private void writeFORMULA(int iRow, int iCol, String formula, XSSFSheet sheet) {
        try {
            Cell cell = getXCell(iRow, iCol, sheet);
            cell.removeFormula();
            cell.setCellFormula(formula);
            evaluator.evaluate(cell);
        } catch (Exception e) {
            log.info(">> unable to set formula to Cell(" + iRow + ", " + iCol + ")");
        }
    }

    private String getSumFormula(int rStart, int cStart, int rEnd, int cEnd) {
        return "SUM(" + xCell(rStart, cStart) + ":" + xCell(rEnd, cEnd) + ")";
    }

    private String xCell(int row, int col) {
        //M transforms JAVA coordinates (e.g. 0,0) into Excel coordinates (e.g. A1)
        return CellReference.convertNumToColString(col + 1) + (row + 1);
    }

    private void setAllRowSum() {
        /*
        for (int i = 6; i < 400; i++) {
            setRowSum(16, 42, i); // ACC
            setRowSum(43, 47, i); // CACQ
            setRowSum(48, 60, i); // DS
            // setRowSum(61, 67, i); // E2E
            // setRowSum(68, 70, i); // CMO
            setRowSum(71, 74, i); // COO SACC
            setRowSum(75, 81, i); // COO PTM WL
            // setRowSum(82, 87, i); // COO PTM SPS
            setRowSum(88, 91, i); // CS WL
            setRowSum(92, 97, i); // CS SPS
            // setRowSum(98, 101, i); // CS OTH
            // setRowSum(102, 109, i); // COO BSA
            setRowSum(110, 111, i); // CMO
            // setRowSum(112, 113, i); // GTM FI
            setRowSum(114, 120, i); // FSO
            // setRowSum(127, 128, i); // PSO
            // setRowSum(129, 131, i); // TSS O
            // setRowSum(132, 133, i); // MTS O
            // setRowSum(134, 135, i); // PO
            setRowSum(136, 146, i); // CO OPX
            setRowSum(147, 149, i); // k€ CO CPX
            // setRowSum(150, 154, i); // NON MS

        }

         */
    }

    private void setRowSum(int colSum, int colEnd, int row) {
        for (XSSFSheet sheet : sheets) {
            int xSum = offset(colSum, sheet);
            int xStart = xSum + 1;
            int xEnd = offset(colEnd, sheet);
            Cell cell = sheet.getRow(row).getCell(xSum);
            try {
                if (cell.getCellFormula()!=null) cell.removeFormula();
                cell.setCellFormula(getSumFormula(row, xStart - 1, row, xEnd - 1));
                evaluator.evaluate(cell);
            } catch (Exception e) {

            }


        }
    }

    private ProjectType giveProjectType(XSSFSheet sheet) {
        if (sheet.equals(np)) {
            return ProjectType.NEW;
        } else {
            if (sheet.equals(ogp)) {
                return ProjectType.ONGOING;
            } else {
                return ProjectType.RUN;
            }
        }
    }

}