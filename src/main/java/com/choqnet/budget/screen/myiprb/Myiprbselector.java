package com.choqnet.budget.screen.myiprb;

import com.choqnet.budget.entity.IPRB;
import com.choqnet.budget.entity.IPRBRegistration;
import com.choqnet.budget.entity.User;
import io.jmix.core.DataManager;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@UiController("Myiprbselector")
@UiDescriptor("MyIPRBSelector.xml")
@DialogMode(width = "1000px", height = "900px")
public class Myiprbselector extends Screen {

    List<String> filters = new ArrayList<>();
    List<IPRB> iprbList;
    User _user;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TextField<String> fltName;
    @Autowired
    private TextField<String> fltReference;
    @Autowired
    private TextField<String> fltOwner;
    @Autowired
    private CollectionContainer<IPRB> iPRBsDc;

    public void setContext(User user) {
        _user = user;
        getIPRBToDisplay();
    }

    private void getIPRBToDisplay() {
        List<IPRB> linkedIPRB = dataManager.load(IPRBRegistration.class)
                .query("select e from IPRBRegistration e where e.user = :user")
                .parameter("user", _user)
                .list()
                .stream()
                .map(IPRBRegistration::getIprb)
                .collect(Collectors.toList());

        List<IPRB> iprbAll = dataManager.load(IPRB.class)
                .query("select e from IPRB e" + getFilterValue()).list();

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
        StringBuilder filter = new StringBuilder();
        int idx = 0;
        for (String query: filters) {
            if (idx==0) {
                filter = new StringBuilder(" where " + query);
            } else {
                filter.append(" and ").append(query);
            }
            idx++;
        }
        return filter.toString();
    }

    @Subscribe("fltName")
    public void onFltNameValueChange(HasValue.ValueChangeEvent<String> event) {
        getIPRBToDisplay();
    }

    @Subscribe("fltOwner")
    public void onFltOwnerValueChange(HasValue.ValueChangeEvent<String> event) {
        getIPRBToDisplay();
    }

    @Subscribe("fltReference")
    public void onFltReferenceValueChange(HasValue.ValueChangeEvent<String> event) {
        getIPRBToDisplay();
    }


}