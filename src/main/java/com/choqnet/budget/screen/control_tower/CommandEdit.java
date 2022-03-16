package com.choqnet.budget.screen.control_tower;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Command;

@UiController("Command.edit")
@UiDescriptor("command-edit.xml")
@EditedEntityContainer("commandDc")
public class CommandEdit extends StandardEditor<Command> {
}