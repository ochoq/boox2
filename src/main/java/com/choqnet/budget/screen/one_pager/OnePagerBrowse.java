package com.choqnet.budget.screen.one_pager;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.OnePager;

@UiController("OnePager.browse")
@UiDescriptor("one-pager-browse.xml")
@LookupComponent("table")
public class OnePagerBrowse extends MasterDetailScreen<OnePager> {
}