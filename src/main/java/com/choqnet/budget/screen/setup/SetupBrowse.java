package com.choqnet.budget.screen.setup;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Setup;

@UiController("Setup.browse")
@UiDescriptor("setup-browse.xml")
@LookupComponent("table")
public class SetupBrowse extends MasterDetailScreen<Setup> {
}