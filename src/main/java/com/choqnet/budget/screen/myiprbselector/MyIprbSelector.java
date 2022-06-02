package com.choqnet.budget.screen.myiprbselector;

import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.entity.User;
import io.jmix.core.DataManager;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@UiController("MyIprbSelector")
@UiDescriptor("my-IPRB-selector.xml")
@DialogMode(width = "1000px", height = "900px")
public class MyIprbSelector extends Screen {

    List<String> filters = new ArrayList<>();
    List<IPRB> iprbList;
    User _user;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private CollectionContainer<IPRB> iPRBsDc;
    @Autowired
    private TextField<String> fltName;
    @Autowired
    private TextField<String> fltReference;
    @Autowired
    private TextField<String> fltOwner;

    public void setContext(User user) {
        //iPRBsDl.load();
        _user = user;
        getIPRBToDisplay();
    }

    private void getIPRBToDisplay() {
        List<IPRB> linkedIPRB = _user.getMyIprbs();
        List<IPRB> iprbAll = dataManager.load(IPRB.class)
                .query("select e from IPRB e" + getFilterValue()).list();
        System.out.println("select e from IPRB e" + getFilterValue());
        List<IPRB> proposed = new ArrayList<>();
        for(IPRB iprb: iprbAll) {
            if (!linkedIPRB.contains(iprb)) {
                proposed.add(iprb);
            }
        }
        iPRBsDc.setItems(proposed);
    }

    @Subscribe("iPRBsTable")
    public void onIPRBsTableSelection(DataGrid.SelectionEvent<IPRB> event) {
        iprbList = new ArrayList<>();
        iprbList.addAll(event.getSelected());
    }

    public List<IPRB> getList() {
        return iprbList;
    }

    @Subscribe("btnClose")
    public void onBtnCloseClick(Button.ClickEvent event) {
        closeWithDefaultAction();
    }

    // *** Filter Area
    private String getFilterValue() {
        // build the list of filters
        filters = new ArrayList<>();
        if (fltName.getValue()!=null) {
            filters.add("UPPER(e.name) like '%" + fltName.getValue().toUpperCase() + "%'");
        }
        if (fltReference.getValue()!=null) {
            filters.add("UPPER(e.reference) like '%" + fltReference.getValue().toUpperCase() + "%'");
        }
        if (fltOwner.getValue()!=null) {
            filters.add("UPPER(e.owner) like '%" + fltOwner.getValue().toUpperCase() + "%'");
        }
        String filter = "";
        int idx = 0;
        for (String query: filters) {
            if (idx==0) {
                filter = " where " + query;
            } else {
                filter += " and " + query;
            }
            idx++;
        }
        return filter;
    }

    @Subscribe("fltName")
    public void onFltNameValueChange(HasValue.ValueChangeEvent event) {
        getIPRBToDisplay();
    }

    @Subscribe("fltOwner")
    public void onFltOwnerValueChange(HasValue.ValueChangeEvent event) {
        getIPRBToDisplay();
    }

    @Subscribe("fltReference")
    public void onFltReferenceValueChange(HasValue.ValueChangeEvent event) {
        getIPRBToDisplay();
    }






}