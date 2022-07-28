package com.choqnet.budget.screen.nbrange;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.NbRange;

@UiController("NbRange.edit")
@UiDescriptor("nb-range-edit.xml")
@EditedEntityContainer("nbRangeDc")
public class NbRangeEdit extends StandardEditor<NbRange> {
}