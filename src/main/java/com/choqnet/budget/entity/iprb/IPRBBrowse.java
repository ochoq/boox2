package com.choqnet.budget.entity.iprb;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.IPRB;

@UiController("IPRB.browse")
@UiDescriptor("iprb-browse.xml")
@LookupComponent("iPRBsTable")
public class IPRBBrowse extends StandardLookup<IPRB> {
}