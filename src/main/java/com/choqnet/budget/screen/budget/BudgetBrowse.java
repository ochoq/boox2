package com.choqnet.budget.screen.budget;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Budget;

@UiController("Budget.browse")
@UiDescriptor("budget-browse.xml")
@LookupComponent("budgetsTable")
public class BudgetBrowse extends StandardLookup<Budget> {

}