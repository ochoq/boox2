package com.choqnet.budget.screen.control_tower;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.app.MainProcessBean;
import com.choqnet.budget.app.UMsgBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import com.choqnet.budget.screen.popups.uploadactuals.UploadActuals;
import com.choqnet.budget.screen.showerrormessage.ShowErrorMessage;
import io.jmix.core.DataManager;
import io.jmix.core.FileStorage;
import io.jmix.core.SaveContext;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@UiController("ControlTower")
@UiDescriptor("control-tower.xml")
public class ControlTower extends Screen {
    private static final Logger log = LoggerFactory.getLogger(ControlTower.class);
    @Autowired
    private UMsgBean umb;
    @Autowired
    private TextArea<String> txtMessage;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
//    @Autowired
//    private ComboBox<Budget> cmbBudget;
    @Autowired
    private FileStorage fileStorage;
    @Autowired
    private PasswordEncoder passwordEncoder;


    private XSSFWorkbook wb;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private Downloader downloader;
    @Autowired
    private UtilBean utilBean;
    @Autowired
    private CollectionLoader<Token> tokensDl;
    @Autowired
    private MainProcessBean mainProcessBean;
    @Autowired
    private RichTextArea rtf;
    @Autowired
    private CollectionLoader<Command> commandsDl;
    @Autowired
    private CollectionLoader<LogApp> logAppsDl;
    @Autowired
    private Label<String> lblTitre;
    @Autowired
    private Dialogs dialogs;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private ComboBox<Budget> cmbBudget;


    // *** Initialisations
    @Subscribe
    public void onInit(InitEvent event) {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
        // loads token
        tokensDl.load();
        // loads message for edition
        rtf.setValue(dataManager.load(Token.class).query("select e from Token e").one().getMessage());
    }


    // *** UI
    @Subscribe("screen")
    public void onScreenSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        switch(event.getSelectedTab().getName()) {
            case "control":
                commandsDl.setQuery("select e from Command e");
                commandsDl.load();
                break;
            case "logApp":
                logAppsDl.setQuery("select e from LogApp e");
                logAppsDl.load();
            default:
                break;
        }
    }

    @Subscribe("commandsTable")
    public void onCommandsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Command> event) {
        dataManager.save(event.getItem());
    }

    @Subscribe("tokTable")
    public void onTokTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Token> event) {
        dataManager.save(event.getItem());
    }

    @Subscribe("mailTable")
    public void onMailTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Token> event) {
        dataManager.save(event.getItem());
    }

    @Subscribe("counterTable")
    public void onCounterTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Token> event) {
        dataManager.save(event.getItem());
    }





    // *** communications


    // *** utilities
    @Subscribe("btnSendMessage")
    public void onBtnSendMessageClick(Button.ClickEvent event) {
        umb.publishEvent(new UserNotification(this, txtMessage.getValue()));
    }

//    @Subscribe("btnExportBudget")
//    public void onBtnExportBudgetClick(Button.ClickEvent event) {
//        // launches the export of the budget Excel file
//        Template template = dataManager.load(Template.class)
//                .query("select e from Template e where e.code = 'MSBUDGET'")
//                .list().get(0);
//        try {
//            wb = new XSSFWorkbook(fileStorage.openStream(template.getFile()));
//            wb = xLUtils.processFile(wb, cmbBudget.getValue());
//            File directory = new File("tmp");
//            directory.mkdir();
//            FileOutputStream outFile = new FileOutputStream("tmp\\tmp_budget.xlsx");
//            wb.write(outFile);
//            outFile.close();
//
//            String filePath = "tmp\\tmp_budget.xlsx";
//            // file to byte[], File -> Path
//            File file2 = new File(filePath);
//            byte[] bytes2 = Files.readAllBytes(file2.toPath());
//            downloader.download(bytes2, "MS Budget - Acceptance.xlsx");
//        } catch (IOException e) {
//            log.error("Error", e);
//        }
//    }

    // DEBUG
    @Subscribe("btnTextExportExcel")
    public void onBtnTextExportExcelClick(Button.ClickEvent event) {
        // launches the export of the budget Excel file
        Template template = dataManager.load(Template.class)
                .query("select e from Template e where e.code = 'MSBUDGET'")
                .list().get(0);
        try {
            wb = new XSSFWorkbook(fileStorage.openStream(template.getFile()));
            wb = xLUtils.processFileDebug(wb, cmbBudget.getValue());

            File directory = new File("tmp");
            directory.mkdir();
            FileOutputStream outFile = new FileOutputStream("tmp\\tmp_budget.xlsx");
            wb.write(outFile);
            outFile.close();

            // displays the list of errors
            if (xLUtils.giveErrorLog().size()>0) {
                ShowErrorMessage sem = screenBuilders.screen(this)
                        .withScreenClass(ShowErrorMessage.class)
                        .withOpenMode(OpenMode.DIALOG)
                        .build();
                sem.show();
            }


            String filePath = "tmp\\tmp_budget.xlsx";
            // file to byte[], File -> Path
            File file2 = new File(filePath);
            byte[] bytes2 = Files.readAllBytes(file2.toPath());
            downloader.download(bytes2, "Test Results.xlsx");
        } catch (IOException e) {
            log.error("Error", e);
        }
    }
    // END DEBUG

    @Subscribe("cleanTeams")
    public void onCleanTeamsClick(Button.ClickEvent event) {
        //
        List<Team> zeroTeam = dataManager.load(Team.class)
                .query("select e from Team e where e.parent is null")
                .list();
        for (Team team: zeroTeam) {
            utilBean.HierarchicalData(team);
        }
    }

    public void createPeople() {
        String input = "amaury;kervyn;akervyn;amaury.kervyn@ingenico.comµanders;ohlander;aohlander;anders.ohlander@worldline.comµArvind;Singh;asingh;Arvind.Singh@ingenico.comµArni;Smit;asmit;Arni.Smit@epay.ingenico.comµAnne-Claude;TICHAUER;atichauer;Anne-Claude.TICHAUER@ingenico.comµbernd;hingst;bhingst;bernd.hingst@six-group.comµCedric;Donckels;cdonckels;Cedric.Donckels@ingenico.comµchrista;hediger;chediger;christa.hediger@worldline.comµcamile;telles;ctelles;camile.telles@worldline.comµchloe;tiberghien;ctiberghien;chloe.tiberghien@ingenico.comµCharam;VAZIRI;cvaziri;Charam.VAZIRI@ingenico.comµDavy;LESPAGNOL;dlespagnol;Davy.LESPAGNOL@ingenico.comµDenis;Simonet;dsimonet;Denis.Simonet@ingenico.comµeline;blomme;eblomme;eline.blomme@worldline.comµEric;PICOU;epicou;Eric.PICOU@ingenico.comµemilia;sandahl;esandahl;emilia.sandahl@bambora.comµFederico;deTogni;fdetogni;Federico.deTogni@epay.ingenico.comµFabrizio;Leonardo;fleonardo;Fabrizio.Leonardo@ingenico.comµGeoffroy;GUEULETTE;ggueulette;Geoffroy.GUEULETTE@ingenico.comµGuido;Hendrickx;ghendrickx;Guido.Hendrickx@ingenico.comµGilles;RAYMOND;graymond;Gilles.RAYMOND@ingenico.comµJagdish;Kumar;jkumar;Jagdish.Kumar@ingenico.comµJerker;lundberg;jlundberg;Jerker.lundberg@bambora.comµjens;stenvall;jstenvall;jens.stenvall@worldline.comµkhalil;kammoun;kkammoun;khalil.kammoun@six-group.comµKaivalya;Paluskar;kpaluskar;Kaivalya.Paluskar@epay.ingenico.comµkoenraad;vanmelkebeke;kvanmelkebeke;koenraad.vanmelkebeke@worldline.comµlefras;coetzee;lcoetzee;lefras.coetzee@worldline.comµLaurent;LEBOEUF;lleboeuf;Laurent.LEBOEUF@ingenico.comµMourad;BEJAR;mbejar;Mourad.BEJAR@ingenico.comµmartin;boulanger;mboulanger;martin.boulanger@equensworldline.comµMarco;Hauff;mhauff;Marco.Hauff@ingenico.comµmichael;huffman;mhuffman;michael.huffman@bambora.comµMohamed;LACHGUER;mlachguer;Mohamed.LACHGUER@ingenico.comµmariehelene;liegeois;mliegeois;mariehelene.liegeois@worldline.comµmike;ohalloran;mohalloran;mike.ohalloran@bambora.comµMinh-Tuan;Pham;mpham;Minh-Tuan.Pham@ingenico.comµmartin;sunnerstig;msunnerstig;martin.sunnerstig@worldline.comµnils;herloffpetersen;nherloffpetersen;nils.herloffpetersen.external@worldline.comµNicolas;Postal;npostal;Nicolas.Postal@ingenico.comµniklas;rosvall;nrosvall;niklas.rosvall@worldline.comµolivier;blerot;oblerot;olivier.blerot@worldline.comµOhad;SOMJEN;osomjen;Ohad.SOMJEN@ingenico.comµPierre-Olivier;CHAPLAIN;pchaplain;Pierre-Olivier.CHAPLAIN@ingenico.comµPaula;Costa;pcosta;Paula.Costa@epay.ingenico.comµpernilla;grabner;pgrabner;pernilla.grabner@bambora.comµpatrik;osterling;posterling;patrik.osterling@worldline.comµPierre;RIAT;priat;Pierre.RIAT@ingenico.comµPierre;SALIGNAT;psalignat;Pierre.SALIGNAT@ingenico.comµPetra;Steenstra;psteenstra;Petra.Steenstra@ingenico.comµPaul;Taylor;ptaylor;Paul.Taylor1@ingenico.comµpeter;tellram;ptellram;peter.tellram@bambora.comµroman;eugster;reugster;roman.eugster@worldline.comµRui;Nunes;rnunes;Rui.Nunes@epay.ingenico.comµrickard;schoultz;rschoultz;rickard.schoultz@worldline.comµRik;vantHof;rvanthof;Rik.vantHof@epay.ingenico.comµsofia;albertsson;salbertsson;sofia.albertsson@bambora.comµstefaan;degeest;sdegeest;stefaan.degeest@worldline.comµSebastien;Fantone;sfantone;Sebastien.Fantone@ingenico.comµShekhar;Kachole;skachole;Shekhar.Kachole@epay.ingenico.comµsarah;lamarque;slamarque;sarah.lamarque@ingenico.comµSebastien;LETNIOWSKI;sletniowski;Sebastien.LETNIOWSKI@ingenico.comµStanley;Macnack;smacnack;Stanley.Macnack@ingenico.comµStephan;VanGulck;svangulck;Stephan.VanGulck@ingenico.comµSimone;vanSchaik;svanschaik;Simone.vanSchaik@ingenico.comµTeun;Boer;tboer;Teun.Boer@ingenico.comµThejus;Philip;tphilip;Thejus.Philip@ingenico.comµWilly;DeReymaeker;wdereymaeker;Willy.DeReymaeker@ingenico.comµZlatko;Bralic;zbralic;Zlatko.Bralic@ingenico.comµ";
        //String input = "Ariana;delRosario;adelrosario;Ariana.delRosario@ingenico.comµAlexandre;DURET;aduret;Alexandre.DURET@ingenico.comµ";
        String[] lines = input.split("µ");
        SaveContext saveContext = new SaveContext();

        for (String line: lines) {
            String[] userProps = line.split(";");
            User user = dataManager.create(User.class);
            user.setFirstName(userProps[0]);
            user.setActive(true);
            user.setLastName(userProps[1]);
            user.setPassword(passwordEncoder.encode("boox"));
            user.setUsername(userProps[2]);
            user.setEmail(userProps[3]);
            saveContext.saving(user);
        }
        dataManager.save(saveContext);
        notifications.create()
                .withDescription("Users created")
                .show();
    }

    @Subscribe("rtf")
    public void onRtfValueChange(HasValue.ValueChangeEvent<String> event) {
        Token token = dataManager.load(Token.class).query("select e from Token e").one();
        token.setMessage(event.getValue());
        dataManager.save(token);
    }


    // TEMP FUNCTIONS FOR TEST

    @Subscribe("btnDumbo")
    public void onTestClick(Button.ClickEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MM yyyy, hh:mm:ss");
        log.info("Starting Manual Dumbo import " + LocalDateTime.now().format(dtf));
        lblTitre.setValue("Running...");
        mainProcessBean.getDumboData();
        log.info("Finishing Manual Dumbo import " + LocalDateTime.now().format(dtf));
        lblTitre.setValue("Interfaces with Jira & Dumbo");
        notifications.create().withDescription("Dumbo import completed").show();
    }

    @Subscribe("btnJira")
    public void onBtnJiraClick(Button.ClickEvent event) {
        mainProcessBean.getJiraTeams();
        notifications.create().withDescription("JIRA's teams import completed").show();
    }

    @Subscribe("btnFireLogs")
    public void onBtnFireLogsClick(Button.ClickEvent event) {
        mainProcessBean.fireLogs();
        notifications.create().withDescription("Logs fired and flushed").show();
    }



    private String getSumFormula(int rStart, int cStart, int rEnd, int cEnd) {
        return "SUM(" + xCell(rStart, cStart) + ":" + xCell(rEnd, cEnd) + ")";
    }

    private String xCell(int row, int col) {
        //M transforms JAVA coordinates (e.g. 0,0) into Excel coordinates (e.g. A1)
        return CellReference.convertNumToColString(col + 1) + (row + 1);
    }

    // *** communications
    // --- General Messaging
    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(">> " + event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }

    @Subscribe("btnCosts")
    public void onBtnCostsClick(Button.ClickEvent event) {
        dialogs.createOptionDialog().withMessage("Are you sure to set the costs for 2022 data?")
                .withCaption("Costs Setup")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e -> setupCosts()),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }

    private void setupCosts() {
        SaveContext sc;
        List<String> months = Arrays.asList("fin202201", "fin202202", "fin202203", "fin202204");
        for (String month: months) {
            log.info("Month "  + month);
            sc = new SaveContext();
            List<Worklog> worklogs = dataManager.load(Worklog.class)
                    .query("select e from Worklog e where e.finMonth = :month")
                    .parameter("month", month)
                    .list();
            for (Worklog worklog: worklogs) {
                worklog.setBudgetCost();
                sc.saving(worklog);
            }
            dataManager.save(sc);
            log.info("--> worklogs done.");
            sc = new SaveContext();
            List<Actual> actuals = dataManager.load(Actual.class)
                    .query("select e from Actual e where e.finMonth = :month")
                    .parameter("month", month)
                    .list();
            for (Actual actual: actuals) {
                actual.setBudgetCost();
                sc.saving(actual);
            }
            dataManager.save(sc);
            log.info("--> actuals done.");
        }
        log.info("Done");
        notifications.create().withCaption("Actual and Worklog Cost Setup Done for " + months.toString() + ".").show();
    }

    @Subscribe("btnDummy")
    public void onBtnDummyClick(Button.ClickEvent event) {
        utilBean.updateDetailProgressAndCapacity();
        notifications.create().withDescription("Done").show();

    }

    @Subscribe("btnUploadActual")
    public void onBtnUploadActualClick(Button.ClickEvent event) {
        UploadActuals uploadActuals = screenBuilders.screen(this)
                .withScreenClass(UploadActuals.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withDescription("Manual Actuals Loader closed. Please consider to launch an update of the capacities, details and progresses.")
                            .withType(Notifications.NotificationType.TRAY)
                            .show();
                })
                .build();
        uploadActuals.show();
    }

//    @Subscribe("btnCutMyIPRB")
//    public void onBtnCutMyIPRBClick(Button.ClickEvent event) {
//        List<IPRBRegistration> all = dataManager.load(IPRBRegistration.class).all().list();
//        dataManager.remove(all);
//        notifications.create().withDescription("All myIPRBs deleted").show();
//
//    }



//    @Subscribe("btnInitSetup")
//    public void onBtnInitSetupClick(Button.ClickEvent event) {
//        List<IntRange> intRanges = dataManager.load(IntRange.class).all().list();
//        dataManager.remove(intRanges);
//        List<NbRange> nbRanges = dataManager.load(NbRange.class).all().list();
//        dataManager.remove(nbRanges);
//
//        // temporary function that initializes the setups' distributions from the current hard-coded data
//        SaveContext sc = new SaveContext();
//        IntRange intRange;
//        NbRange nbRange;
//        //List<Setup> setups = dataManager.load(Setup.class).query("select e from Setup e").fetchPlan("setups").list();
//        List<Setup> setups = dataManager.load(Setup.class).all().list();
//        List<IntRange> newRates;
//        List<NbRange> newWorkDays;
//        for (Setup setup: setups) {
//            newRates = new ArrayList<>();
//            newWorkDays = new ArrayList<>();
//
//            int value1 = setup.getR2022Q1();
//            intRange = dataManager.create(IntRange.class);
//            intRange.setSetup(setup);
//            intRange.setDate(LocalDate.of(2022,1,1));
//            intRange.setValue(value1);
//            dataManager.save(intRange);
//            newRates.add(intRange);
//
//            int value2 = setup.getR2022Q2();
//            if (value2!=value1) {
//                intRange = dataManager.create(IntRange.class);
//                intRange.setSetup(setup);
//                intRange.setDate(LocalDate.of(2022,4,1));
//                intRange.setValue(value2);
//                dataManager.save(intRange);
//                newRates.add(intRange);
//            }
//
//
//            int value3 = setup.getWd2022();
//            nbRange = dataManager.create(NbRange.class);
//            nbRange.setSetupNB(setup);
//            nbRange.setDate(LocalDate.of(2022,1,1));
//            nbRange.setValue(value3);
//            dataManager.save(nbRange);
//            newWorkDays.add(nbRange);
//
//            setup.setRates(newRates);
//            setup.setWorkDays(newWorkDays);
//            sc.saving(setup);
//        }
//        dataManager.save(sc);
//        notifications.create().withDescription("Setups initialized").show();
//    }



}