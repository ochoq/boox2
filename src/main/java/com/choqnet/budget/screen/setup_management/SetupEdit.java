package com.choqnet.budget.screen.setup_management;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Setup;

@UiController("Setup.edit")
@UiDescriptor("setup-edit.xml")
@EditedEntityContainer("setupDc")
public class SetupEdit extends StandardEditor<Setup> {
}