package com.choqnet.budget.screen.xlactual;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.XLActual;

@UiController("XLActual.browse")
@UiDescriptor("xl-actual-browse.xml")
@LookupComponent("xLActualsTable")
public class XLActualBrowse extends StandardLookup<XLActual> {
}