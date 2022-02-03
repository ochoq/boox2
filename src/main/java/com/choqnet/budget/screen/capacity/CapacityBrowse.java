package com.choqnet.budget.screen.capacity;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Capacity;

@UiController("Capacity.browse")
@UiDescriptor("capacity-browse.xml")
@LookupComponent("capacitiesTable")
public class CapacityBrowse extends StandardLookup<Capacity> {
}