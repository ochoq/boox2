package com.choqnet.budget.screen.changes;

import com.choqnet.budget.entity.ChangeHistory;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.DialogMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("Changes")
@UiDescriptor("changes.xml")
@DialogMode(width = "50%", height = "50%")
public class Changes extends Screen {
    @Autowired
    private CollectionContainer<ChangeHistory> changeHistoriesDc;
    @Autowired
    private Table<ChangeHistory> changeHistoriesTable;

    public void setData(List<ChangeHistory> changeHistoryList) {
        changeHistoriesDc.setItems(changeHistoryList);
        changeHistoriesTable.repaint();
    }
}