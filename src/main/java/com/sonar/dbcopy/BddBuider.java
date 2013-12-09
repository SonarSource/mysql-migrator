/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.util.Arrays;

public class BddBuider {

  private Bdd bdd =null;

  public BddBuider (){
    bdd = new Bdd("javaBdd");
  }
  /* SETTERS */
  public void addTableToBdd (){
    bdd.setBddTables(Arrays.asList(
      new Table("action_plans"),
      new Table("active_dashboards"),
      new Table("active_rule_changes"),
      new Table("active_rule_notes"),
      new Table("active_rule_param_changes"),
      new Table("active_rule_parameters"),
      new Table("active_rules"),
      new Table("alerts"),
      new Table("authors"),
      new Table("characteristic_edges"),
      new Table("characteristic_properties"),
      new Table("characteristics"),
      new Table("dashboards"),
      new Table("dependencies"),
      new Table("duplications_index"),
      new Table("events"),
      new Table("graphs"),
      new Table("group_roles"),
      new Table("groups"),
      new Table("groups_users"),
      new Table("issue_changes"),
      new Table("issue_filter_favourites"),
      new Table("issue_filters"),
      new Table("issues"),
      new Table("loaded_templates"),
      new Table("manual_measures"),
      new Table("measure_data"),
      new Table("measure_filter_favourites"),
      new Table("measure_filters"),
      new Table("metrics"),
      new Table("notifications"),
      new Table("perm_templates_groups"),
      new Table("perm_templates_users"),
      new Table("permission_templates"),
      new Table("project_links"),
      new Table("project_measures"),
      new Table("projects"),
      new Table("properties"),
      new Table("quality_models"),
      new Table("resource_index"),
      new Table("rule_notes"),
      new Table("rules"),
      new Table("rules_parameters"),
      new Table("rules_profiles"),
      new Table("schema_migrations"),
      new Table("semaphores"),
      new Table("snapshot_data"),
      new Table("snapshot_sources"),
      new Table("snapshots"),
      new Table("user_roles"),
      new Table("users"),
      new Table("widget_properties"),
      new Table("widgets")
    ));
  }
  /* GETTERS */
  public Bdd getBdd(){
    return this.bdd;
  }
}
