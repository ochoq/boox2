package com.choqnet.budget.screen.actual;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Actual;

@UiController("Actual.browse")
@UiDescriptor("actual-browse.xml")
@LookupComponent("actualsTable")
public class ActualBrowse extends StandardLookup<Actual> {
}