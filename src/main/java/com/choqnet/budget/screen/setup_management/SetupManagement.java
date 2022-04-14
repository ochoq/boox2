package com.choqnet.budget.screen.setup_management;

import com.choqnet.budget.entity.Setup;
import io.jmix.core.DataManager;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("SetupManagement")
@UiDescriptor("setup-management.xml")
public class SetupManagement extends Screen {
    @Autowired
    private CollectionLoader<Setup> setupsDl;
    @Autowired
    private DataManager dataManager;

    @Subscribe
    public void onInit(InitEvent event) {
        setupsDl.load();
    }

    @Subscribe("setupsTable")
    public void onSetupsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Setup> event) {
        dataManager.save(event.getItem());
    }
}