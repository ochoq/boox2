package com.choqnet.budget.screen.control_tower;

import com.choqnet.budget.UtilBean;
import com.choqnet.budget.XLUtils;
import com.choqnet.budget.app.UMsgBean;
import com.choqnet.budget.communications.UserNotification;
import com.choqnet.budget.entity.Budget;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.entity.Template;
import com.choqnet.budget.entity.User;
import io.jmix.core.DataManager;
import io.jmix.core.FileStorage;
import io.jmix.core.SaveContext;
import io.jmix.core.security.PasswordNotMatchException;
import io.jmix.core.security.UserManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

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

    private XSSFWorkbook wb;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private Downloader downloader;
    @Autowired
    private UtilBean utilBean;


    // *** Initialisations
    @Subscribe
    public void onInit(InitEvent event) {
        List<Budget> budgets = dataManager.load(Budget.class)
                .query("select e from Budget e order by e.year desc, e.name asc")
                .list();
        cmbBudget.setOptionsList(budgets);
        Optional<Budget> prefBudget = budgets.stream().filter(Budget::getPreferred).findFirst();
        prefBudget.ifPresent(value -> cmbBudget.setValue(value));
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

   /* @Subscribe("createPeople")
    public void onCreatePeopleClick(Button.ClickEvent event) {
        String input = "amaury;kervyn;akervyn;amaury.kervyn@ingenico.comµPierre-Olivier;CHAPLAIN;pchaplain;Pierre-Olivier.CHAPLAIN@ingenico.comµmartin;sunnerstig;msunnerstig;martin.sunnerstig@worldline.comµJerker;lundberg;jlundberg;Jerker.lundberg@bambora.comµSylvain;MARION;smarion;Sylvain.MARION@ingenico.comµolivier;blerot;oblerot;olivier.blerot@worldline.comµMinh-Tuan;Pham;mpham;Minh-Tuan.Pham@ingenico.comµmike;ohalloran;mohalloran;mike.ohalloran@bambora.comµGilles;RAYMOND;graymond;Gilles.RAYMOND@ingenico.comµAnne-Claude;TICHAUER;atichauer;Anne-Claude.TICHAUER@ingenico.comµMourad;BEJAR;mbejar;Mourad.BEJAR@ingenico.comµRui;Nunes;rnunes;Rui.Nunes@epay.ingenico.comµTeun;Boer;tboer;Teun.Boer@ingenico.comµEric;PICOU;epicou;Eric.PICOU@ingenico.comµCharam;VAZIRI;cvaziri;Charam.VAZIRI@ingenico.comµArvind;Singh;asingh;Arvind.Singh@ingenico.comµMohamed;LACHGUER;mlachguer;Mohamed.LACHGUER@ingenico.comµIsmael;JAAFAR;ijaafar;Ismael.JAAFAR@ingenico.comµMarco;Hauff;mhauff;Marco.Hauff@ingenico.comµMartin;TOMKINS;mtomkins;Martin.TOMKINS@ingenico.comµNicolas;Postal;npostal;Nicolas.Postal@ingenico.comµeline;blomme;eblomme;eline.blomme@worldline.comµGeoffroy;GUEULETTE;ggueulette;Geoffroy.GUEULETTE@ingenico.comµJorge;TARLEA;jtarlea;Jorge.TARLEA@ingenico.comµSebastien;Fantone;sfantone;Sebastien.Fantone@ingenico.comµPaul;Taylor;ptaylor;Paul.Taylor1@ingenico.comµchrista;hediger;chediger;christa.hediger@worldline.comµPierre;RIAT;priat;Pierre.RIAT@ingenico.comµjens;stenvall;jstenvall;jens.stenvall@worldline.comµOhad;SOMJEN;osomjen;Ohad.SOMJEN@ingenico.comµmartin;boulanger;mboulanger;martin.boulanger@equensworldline.comµThejus;Philip;tphilip;Thejus.Philip@ingenico.comµDenis;Simonet;dsimonet;Denis.Simonet@ingenico.comµMichael;AMSELLEM;mamsellem;Michael.AMSELLEM@ingenico.comµShekhar;Kachole;skachole;Shekhar.Kachole@epay.ingenico.comµRik;vantHof;rvanthof;Rik.vantHof@epay.ingenico.comµchloe;tiberghien;ctiberghien;chloe.tiberghien@ingenico.comµroman;eugster;reugster;roman.eugster@worldline.comµCedric;Donckels;cdonckels;Cedric.Donckels@ingenico.comµlefras;coetzee;lcoetzee;lefras.coetzee@worldline.comµSebastien;LETNIOWSKI;sletniowski;Sebastien.LETNIOWSKI@ingenico.comµPierre;SALIGNAT;psalignat;Pierre.SALIGNAT@ingenico.comµZlatko;Bralic;zbralic;Zlatko.Bralic@ingenico.comµKaivalya;Paluskar;kpaluskar;Kaivalya.Paluskar@epay.ingenico.comµkhalil;kammoun;kkammoun;khalil.kammoun@six-group.comµJagdish;Kumar;jkumar;Jagdish.Kumar@ingenico.comµThierry;FORTUNE;tfortune;Thierry.FORTUNE@ingenico.comµmariehelene;liegeois;mliegeois;mariehelene.liegeois@worldline.comµpernilla;grabner;pgrabner;pernilla.grabner@bambora.comµAlexandre;DURET;aduret;Alexandre.DURET@ingenico.comµSimone;vanSchaik;svanschaik;Simone.vanSchaik@ingenico.comµanders;ohlander;aohlander;anders.ohlander@worldline.comµcamile;telles;ctelles;camile.telles@worldline.comµrickard;schoultz;rschoultz;rickard.schoultz@worldline.comµFederico;deTogni;fdetogni;Federico.deTogni@epay.ingenico.comµniklas;rosvall;nrosvall;niklas.rosvall@worldline.comµPetra;Steenstra;psteenstra;Petra.Steenstra@ingenico.comµstefaan;degeest;sdegeest;stefaan.degeest@worldline.comµmichael;huffman;mhuffman;michael.huffman@bambora.comµDavy;LESPAGNOL;dlespagnol;Davy.LESPAGNOL@ingenico.comµemilia;sandahl;esandahl;emilia.sandahl@bambora.comµLaurent;LEBOEUF;lleboeuf;Laurent.LEBOEUF@ingenico.comµStanley;Macnack;smacnack;Stanley.Macnack@ingenico.comµbernd;hingst;bhingst;bernd.hingst@six-group.comµWilly;DeReymaeker;wdereymaeker;Willy.DeReymaeker@ingenico.comµAriana;delRosario;adelrosario;Ariana.delRosario@ingenico.comµPaula;Costa;pcosta;Paula.Costa@epay.ingenico.comµsarah;lamarque;slamarque;sarah.lamarque@ingenico.comµFabrizio;Leonardo;fleonardo;Fabrizio.Leonardo@ingenico.comµpeter;tellram;ptellram;peter.tellram@bambora.comµkoenraad;vanmelkebeke;kvanmelkebeke;koenraad.vanmelkebeke@worldline.comµGuido;Hendrickx;ghendrickx;Guido.Hendrickx@ingenico.comµnils;herloffpetersen;nherloffpetersen;nils.herloffpetersen.external@worldline.comµStephan;VanGulck;svangulck;Stephan.VanGulck@ingenico.comµsofia;albertsson;salbertsson;sofia.albertsson@bambora.comµpatrik;osterling;posterling;patrik.osterling@worldline.comµArni;Smit;asmit;Arni.Smit@epay.ingenico.comµCharlotte;PERRAUDIN;cperraudin;Charlotte.PERRAUDIN@ingenico.comµ";
        String[] lines = input.split("µ");
        SaveContext saveContext = new SaveContext();
        for (String line: lines) {
            String[] userProps = line.split(";");
            User user = dataManager.create(User.class);
            user.setFirstName(userProps[0]);
            user.setActive(true);
            user.setLastName(userProps[1]);
            user.setPassword("boox");
            user.setUsername(userProps[2]);
            user.setEmail(userProps[3]);
            saveContext.saving(user);
        }
        dataManager.save(saveContext);
        notifications.create()
                .withDescription("Users created")
                .show();
    }*/



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


}