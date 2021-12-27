package com.choqnet.budget.screen.mapping;

import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Mapping.browse")
@UiDescriptor("mapping-browse.xml")
@LookupComponent("mappingsTable")
public class MappingBrowse extends StandardLookup<Mapping> {
    @Autowired
    private Button btnRemove;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CollectionLoader<Mapping> mappingsDl;
    @Autowired
    private DataGrid<Mapping> mappingsTable;
    @Autowired
    private Button btnNext;


    @Subscribe
    public void onInit(InitEvent event) {
        btnRemove.setEnabled(false);
        btnNext.setEnabled(false);
        mappingsDl.load();
    }

    @Subscribe("mappingsTable")
    public void onMappingsTableSelection(DataGrid.SelectionEvent<Mapping> event) {
        btnRemove.setEnabled(true);
        btnNext.setEnabled(true);
    }

    @Subscribe("btnCreate")
    public void onBtnCreateClick(Button.ClickEvent event) {
        Mapping mapping = dataManager.create(Mapping.class);
        dataManager.save(mapping);
        mappingsDl.load();
    }

    @Subscribe("btnRemove")
    public void onBtnRemoveClick(Button.ClickEvent event) {
        dataManager.remove(mappingsTable.getSelected());
        mappingsDl.load();
    }

    @Subscribe("mappingsTable")
    public void onMappingsTableEditorPostCommit(DataGrid.EditorPostCommitEvent<Mapping> event) {
        dataManager.save(event.getItem());
        mappingsDl.load();
    }

    @Subscribe("btnNext")
    public void onBtnNextClick(Button.ClickEvent event) {
        Mapping mapping = dataManager.create(Mapping.class);
        mapping.setColNP(mappingsTable.getSingleSelected().getColNP() + 1);
        mapping.setColOGP(mappingsTable.getSingleSelected().getColOGP() + 1);
        mapping.setColRUN(mappingsTable.getSingleSelected().getColRUN() + 1);
        dataManager.save(mapping);
        mappingsDl.load();
    }




}