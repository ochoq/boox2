package com.choqnet.budget.screen.teams;

import com.choqnet.budget.entity.Capacity;
import com.choqnet.budget.entity.Team;
import com.choqnet.budget.screen.popups.parent_chooser.ParentChooser;
import com.choqnet.budget.screen.popups.upload_teams.UploadTeams;
import io.jmix.core.DataManager;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.action.list.RefreshAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@UiController("Teams")
@UiDescriptor("teams.xml")
public class Teams extends Screen {
    @Autowired
    private CollectionLoader<Team> teamsDl;
    @Autowired
    private TreeDataGrid<Team> table;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Dialogs dialogs;
    @Named("table.refresh")
    private RefreshAction tableRefresh;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Filter filter;

    private Team oldParent;
    private String oldName;

    // *** UI functions
    @Subscribe
    public void onInit(InitEvent event) {
        teamsDl.load();
        table.expandAll();
        filter.setExpanded(false);
    }

    @Install(to = "table.parent", subject="editFieldGenerator")
    private Field<Team> editingParent (
            DataGrid.EditorFieldGenerationContext<Team> editorParent) {
        // make the field parent displayed as a list
        // proposed items are teams, not child of the current team
        ComboBox<Team> cb = uiComponents.create(ComboBox.NAME);
        List<Team> restricted  = dataManager
                .load(Team.class).query("select e from Team e").list();
        // removes current team's children from the list. Skipped if no team selected
        if (table.getSingleSelected()!=null && table.getSingleSelected().getFullName()!= null) {
            restricted = restricted.stream()
                    .filter(e -> e.getFullName()!=null && !e.getFullName().contains(table.getSingleSelected().getFullName()))
                    .collect(Collectors.toList());
        }
        // sorting by fullName
        restricted = restricted.stream().sorted(Comparator.comparing(Team::getFullName)).collect(Collectors.toList());
        cb.setValueSource((ValueSource<Team>) editorParent.getValueSourceProvider()
                .getValueSource("parent"));
        cb.setOptionsList(restricted);
        return cb;
    }

    @Subscribe("btnMove")
    public void onBtnMoveClick(Button.ClickEvent event) {
        // sends the selected items to the move pop-up
        ParentChooser parentChooser = screenBuilders.screen(this)
                .withScreenClass(ParentChooser.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    teamsDl.load();
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("Parent Chooser closed.")
                            .show();
                    teamsDl.load();
                })
                .build();
        parentChooser.show();
        parentChooser.getTeamList(table.getSelected());
    }

    @Subscribe("btnUpload")
    public void onBtnUploadClick(Button.ClickEvent event) {
        // opens the dialog screen for uploading the Excel file
        UploadTeams uploadTeams = screenBuilders.screen(this)
                .withScreenClass(UploadTeams.class)
                .withOpenMode(OpenMode.DIALOG)
                .withAfterCloseListener(e -> {
                    notifications.create()
                            .withType(Notifications.NotificationType.TRAY)
                            .withCaption("File upload utility closed.")
                            .show();
                    teamsDl.load();
                })
                .build();
        uploadTeams.show();
    }

    // *** data functions
    @Subscribe("btnAdd")
    public void onBtnAddClick(Button.ClickEvent event) {
        // create a new Team and connect it to the selected item, if any
        Team newTeam = dataManager.create(Team.class);
        Team parent = table.getSingleSelected();
        newTeam.setParent(table.getSingleSelected());
        newTeam.setName("_");
        newTeam.setFullName(parent==null ? "_" : parent.getFullName() +  "-" +"new");
        dataManager.save(newTeam);
        teamsDl.load();
        table.expand(table.getSingleSelected());
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        // delete the selected teams, after confirmation
        dialogs.createOptionDialog()
                .withCaption("Confirmation Requested")
                .withMessage("The selected teams will be permanently deleted. Do you confirm?")
                .withActions(
                        new DialogAction(DialogAction.Type.OK)
                                .withHandler(e -> {
                                    // actual deletion of teams
                                    for (Team team: table.getSelected()) {
                                        // deletion of the attached capacities
                                        List<Capacity> capacities = dataManager.load(Capacity.class)
                                                .query("select e from Capacity e where e.team = :team")
                                                .parameter("team", team)
                                                .list();
                                        dataManager.remove(capacities);
                                        dataManager.remove(team);
                                    }
                                    teamsDl.load();
                                }),
                        new DialogAction(DialogAction.Type.CANCEL)
                )
                .show();
    }

    @Subscribe("table")
    public void onTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Team> event) {
        // saves actually the changes in the DB
        Team target = event.getItem();
        target.setUpdated(LocalDateTime.now());
        target = dataManager.save(target);
        if (
                (oldParent!=null && !oldParent.equals(target.getParent())) || (oldParent==null && target.getParent()!=null)
                || !oldName.equals(target.getName())

        ) {
            propagateFullName(target);
        }
        teamsDl.load();
        tableRefresh.execute();
        table.expand(table.getSingleSelected());
    }

    @Subscribe("table")
    public void onTableEditorOpen(DataGrid.EditorOpenEvent<Team> event) {
        oldParent = event.getItem().getParent();
        oldName = event.getItem().getName();
    }

    private void propagateFullName(Team team) {
        team.setFullName(team.getParent()==null ? team.getName() : team.getParent().getFullName() + "-" + team.getName());
        dataManager.save(team);
        List<Team> children = dataManager.load(Team.class)
                .query("select e from Team e where e.parent = :parent")
                .parameter("parent", team)
                .list();
        for (Team child: children) {
            propagateFullName(child);
        }
    }




}