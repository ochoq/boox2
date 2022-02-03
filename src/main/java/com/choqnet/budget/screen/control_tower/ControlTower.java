package com.choqnet.budget.screen.control_tower;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.app.MainProcessBean;
import com.choqnet.budget.app.UMsgBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.*;
import io.jmix.core.DataManager;
import io.jmix.core.FileStorage;
import io.jmix.core.SaveContext;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.*;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@UiController("ControlTower")
@UiDescriptor("control-tower.xml")
public class ControlTower extends Screen {
    @Autowired
    private UMsgBean umb;
    @Autowired
    private TextArea<String> txtMessage;
    @Autowired
    private Notifications notifications;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private ComboBox<Budget> cmbBudget;
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


    // *** communications


    // *** utilities
    @Subscribe("btnSendMessage")
    public void onBtnSendMessageClick(Button.ClickEvent event) {
        umb.publishEvent(new UserNotification(this, txtMessage.getValue()));
    }

    @Subscribe("btnExportBudget")
    public void onBtnExportBudgetClick(Button.ClickEvent event) {
        // launches the export of the budget Excel file
        Template template = dataManager.load(Template.class)
                .query("select e from Template e where e.code = 'MSBUDGET'")
                .list().get(0);
        try {
            wb = new XSSFWorkbook(fileStorage.openStream(template.getFile()));
            wb = xLUtils.processFile(wb, cmbBudget.getValue());
            File directory = new File("tmp");
            directory.mkdir();
            FileOutputStream outFile = new FileOutputStream("tmp\\tmp_budget.xlsx");
            wb.write(outFile);
            outFile.close();

            String filePath = "tmp\\tmp_budget.xlsx";
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            // file to byte[], File -> Path
            File file2 = new File(filePath);
            byte[] bytes2 = Files.readAllBytes(file2.toPath());
            downloader.download(bytes2, "MS Budget - Acceptance.xlsx");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Subscribe("cleanTeams")
    public void onCleanTeamsClick(Button.ClickEvent event) {
        //
        List<Team> zeroTeam = dataManager.load(Team.class)
                .query("select e from Team e where e.parent is null")
                .list();
        System.out.println("liste " + zeroTeam.size());
        for (Team team: zeroTeam) {
            utilBean.HierarchicalData(team);
        }
    }

    @Subscribe("btnTeams")
    public void onBtnTeamsClick(Button.ClickEvent event) {
        mainProcessBean.getJiraTeams();
        notifications.create().withDescription("Teams imported.").show();

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


    /*
    @EventListener
    private void getUMessage(UserNotification event) {
        notifications.create()
                .withDescription(event.getMessage())
                .withCaption("System Message")
                .withType(Notifications.NotificationType.SYSTEM)
                .show();
    }

    @EventListener
    private void received(UserNotification event) {
        notifications.create()
                .withCaption("System Communication")
                .withDescription(event.getMessage())
                .withType(Notifications.NotificationType.WARNING)
                .withPosition(Notifications.Position.TOP_CENTER)
                .show();
    }

     */

    // *** actual saving of changes made in message



}