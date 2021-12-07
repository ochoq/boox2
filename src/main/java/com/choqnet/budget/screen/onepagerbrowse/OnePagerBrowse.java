package com.choqnet.budget.screen.onepagerbrowse;

import io.jmix.ui.component.Filter;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("OnePagerBrowse")
@UiDescriptor("one-pager-browse.xml")
public class OnePagerBrowse extends Screen {
    @Autowired
    private Filter filter;

    @Subscribe
    public void onInit(InitEvent event) {
        filter.setExpanded(false);
    }
}