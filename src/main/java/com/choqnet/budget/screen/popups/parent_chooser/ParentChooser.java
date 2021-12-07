package com.choqnet.budget.screen.popups.parent_chooser;

import com.choqnet.budget.entity.Team;
import io.jmix.core.DataManager;
import io.jmix.core.SaveContext;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@UiController("ParentChooser")
@UiDescriptor("parent-chooser.xml")
public class ParentChooser extends Screen {
    private Set<Team> teamList;
    private List<Team> parentList;
    private List<Team> toExclude = new ArrayList<>();
    @Autowired
    private DataManager dataManager;
    @Autowired
    private ComboBox<Team> cmbParent;
    @Autowired
    private Button btnConnect;

    public void getTeamList(Set<Team> teams) {
        // populates the combo's option with all teams but the children of the current selection
        this.teamList = teams;
        parentList = dataManager.load(Team.class).all().list();
        for (Team team: teamList) {
            for (Team parent: parentList) {
                if (parent.getFullName().contains(team.getFullName())) {
                    toExclude.add(parent);
                }
            }
        }
        for (Team excluded: toExclude) {
            parentList.remove(excluded);
        }
        cmbParent.setOptionsList(parentList);
    }

    // *** UI functions
    @Subscribe("btnCancel")
    public void onBtnCancelClick(Button.ClickEvent event) {
        // close the current window
        closeWithDefaultAction();
    }

    @Subscribe("btnConnect")
    public void onBtnConnectClick(Button.ClickEvent event) {
        if (cmbParent.getValue()!= null) {
            SaveContext sc = new SaveContext();
            for (Team team: teamList) {
                team.setParent(cmbParent.getValue());
                sc.saving(team);
            }
            dataManager.save(sc);
        }
        closeWithDefaultAction();
    }

    @Subscribe("cmbParent")
    public void onCmbParentValueChange(HasValue.ValueChangeEvent<Team> event) {
        btnConnect.setEnabled(cmbParent.getValue()!=null);
    }
}