package com.choqnet.budget.screen.team;

import io.jmix.ui.screen.*;
import com.choqnet.budget.entity.Team;

@UiController("Team.browse")
@UiDescriptor("team-browse.xml")
@LookupComponent("teamsTable")
public class TeamBrowse extends StandardLookup<Team> {
}