package com.choqnet.budget.screen.detail;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Detail;

@UiController("Detail.browse")
@UiDescriptor("detail-browse.xml")
@LookupComponent("detailsTable")
public class DetailBrowse extends StandardLookup<Detail> {
}