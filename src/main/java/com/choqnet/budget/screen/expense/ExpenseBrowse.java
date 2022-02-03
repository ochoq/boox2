package com.choqnet.budget.screen.expense;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Expense;

@UiController("Expense.browse")
@UiDescriptor("expense-browse.xml")
@LookupComponent("expensesTable")
public class ExpenseBrowse extends StandardLookup<Expense> {
}