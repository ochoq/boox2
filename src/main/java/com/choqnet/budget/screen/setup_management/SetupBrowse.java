package com.choqnet.budget.screen.setup_management;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Setup;

@UiController("Setup.browse")
@UiDescriptor("setup-browse.xml")
@LookupComponent("setupsTable")
public class SetupBrowse extends StandardLookup<Setup> {
}