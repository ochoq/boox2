package com.choqnet.budget.screen.progress;

import com.choqnet.budget.entity.Detail;
import io.jmix.core.DataManager;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Progress;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("Progress.browse")
@UiDescriptor("progress-browse.xml")
@LookupComponent("table")
public class ProgressBrowse extends MasterDetailScreen<Progress> {
    @Autowired
    private DataManager dataManager;
}