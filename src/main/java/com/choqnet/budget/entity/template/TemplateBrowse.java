package com.choqnet.budget.entity.template;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Template;

@UiController("Template.browse")
@UiDescriptor("template-browse.xml")
@LookupComponent("table")
public class TemplateBrowse extends MasterDetailScreen<Template> {
}