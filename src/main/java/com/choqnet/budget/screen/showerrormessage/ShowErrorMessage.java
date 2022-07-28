package com.choqnet.budget.screen.showerrormessage;

import com.choqnet.budget.XLUtils;
import com.choqnet.budget.entity.ErrorMessage;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@UiController("ShowErrorMessage")
@UiDescriptor("show-error-message.xml")
@DialogMode(width = "750px", height = "500px")
public class ShowErrorMessage extends Screen {
    @Autowired
    private CollectionLoader<ErrorMessage> errorMessagesDl;
    @Autowired
    private XLUtils xLUtils;
    @Autowired
    private CollectionContainer<ErrorMessage> errorMessagesDc;

    @Subscribe
    public void onInit(InitEvent event) {
        List<ErrorMessage> log = xLUtils.giveErrorLog();
        errorMessagesDc.setItems(log);
    }

}