package com.choqnet.budget.screen.demand;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Demand;

@UiController("Demand.browse")
@UiDescriptor("demand-browse.xml")
@LookupComponent("demandsTable")
public class DemandBrowse extends StandardLookup<Demand> {
}