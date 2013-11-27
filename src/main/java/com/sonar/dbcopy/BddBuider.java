/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.Arrays;

public class BddBuider {

  protected SonarBDD sonarBDD=null;

    public BddBuider (){
      sonarBDD = new SonarBDD("sonar");
      addtableToBDD();
    }

    public void addtableToBDD (){
      sonarBDD.setBddTables(Arrays.asList(
        new SonarTable("action_plans"),
        new SonarTable("active_dashboards"),
        new SonarTable("active_rule_changes"),
        new SonarTable("active_rule_notes"),
        new SonarTable("active_rule_param_changes"),
        new SonarTable("active_rule_parameters"),
        new SonarTable("active_rules"),
        new SonarTable("alerts"),
        new SonarTable("authors"),
        new SonarTable("characteristic_edges"),
        new SonarTable("characteristic_properties"),
        new SonarTable("characteristics"),
        new SonarTable("dashboards"),
        new SonarTable("dependencies"),
        new SonarTable("duplications_index"),
        new SonarTable("events"),
        new SonarTable("graphs"),
        new SonarTable("group_roles"),
        new SonarTable("groups"),
        new SonarTable("groups_users"),
        new SonarTable("issue_changes"),
        new SonarTable("issue_filter_favourites"),
        new SonarTable("issue_filters"),
        new SonarTable("issues"),
        new SonarTable("loaded_templates"),
        new SonarTable("manual_measures"),
        new SonarTable("measure_data"),
        new SonarTable("measure_filter_favourites"),
        new SonarTable("measure_filters"),
        new SonarTable("metrics"),
        new SonarTable("notifications"),
        new SonarTable("perm_templates_groups"),
        new SonarTable("perm_templates_users"),
        new SonarTable("permission_templates"),
        new SonarTable("project_links"),
        new SonarTable("project_measures"),
        new SonarTable("projects"),
        new SonarTable("properties"),
        new SonarTable("quality_models"),
        new SonarTable("resource_index"),
        new SonarTable("rule_notes"),
        new SonarTable("rules"),
        new SonarTable("rules_parameters"),
        new SonarTable("rules_profiles"),
        new SonarTable("schema_migrations"),
        new SonarTable("semaphores"),
        new SonarTable("snapshot_data"),
        new SonarTable("snapshot_sources"),
        new SonarTable("snapshots"),
        new SonarTable("user_roles"),
        new SonarTable("users"),
        new SonarTable("widget_properties"),
        new SonarTable("widgets")
      ));
    }
}
