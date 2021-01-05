/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.sqdbmigrator.migrator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TablesAndVersionRegistry {

  // hardcoded list of tables per version, generated by scripts/tables-and-version/gen-java.sh
  // from files generated by scripts/extract-tables-and-version.sh
  static final Map<Integer, List<String>> TABLES_PER_VERSION = new HashMap<>();

  static {
    // SonarQube 6.7.7
    TABLES_PER_VERSION.put(1838, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "rules", "rules_metadata", "events", "quality_gates",
      "quality_gate_conditions", "properties", "project_links", "duplications_index", "project_measures", "internal_properties", "projects", "manual_measures", "active_rules",
      "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "loaded_templates", "issues", "issue_changes", "permission_templates",
      "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity", "ce_task_characteristics",
      "ce_task_input", "ce_scanner_context", "user_tokens", "webhook_deliveries", "es_queue", "plugins", "project_branches", "analysis_properties"));

    // SonarQube 6.7
    TABLES_PER_VERSION.put(1837, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "rules", "rules_metadata", "events", "quality_gates",
      "quality_gate_conditions", "properties", "project_links", "duplications_index", "project_measures", "internal_properties", "projects", "manual_measures", "active_rules",
      "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "loaded_templates", "issues", "issue_changes", "permission_templates",
      "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity", "ce_task_characteristics",
      "ce_task_input", "ce_scanner_context", "user_tokens", "webhook_deliveries", "es_queue", "plugins", "project_branches", "analysis_properties"));

    // SonarQube 7.0
    TABLES_PER_VERSION.put(1923, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "rules", "rules_metadata", "events", "quality_gates",
      "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures", "project_measures", "internal_properties", "projects",
      "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "issues", "issue_changes", "permission_templates",
      "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity", "ce_task_characteristics",
      "ce_task_input", "ce_scanner_context", "user_tokens", "webhook_deliveries", "es_queue", "plugins", "project_branches", "analysis_properties"));

    // SonarQube 7.1
    TABLES_PER_VERSION.put(2023, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures", "project_measures",
      "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "issues", "issue_changes",
      "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity",
      "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "user_tokens", "es_queue", "plugins", "project_branches", "analysis_properties", "webhooks",
      "webhook_deliveries"));

    // SonarQube 7.2
    TABLES_PER_VERSION.put(2129, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures", "project_measures",
      "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "issues", "issue_changes",
      "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity",
      "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "user_tokens", "es_queue", "plugins", "project_branches", "analysis_properties", "webhooks",
      "webhook_deliveries", "alm_app_installs", "project_mappings"));

    // SonarQube 7.3
    TABLES_PER_VERSION.put(2211, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures", "project_measures",
      "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "issues", "issue_changes",
      "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity",
      "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "user_tokens", "es_queue", "plugins", "project_branches", "analysis_properties", "webhooks",
      "webhook_deliveries", "alm_app_installs", "project_mappings"));

    // SonarQube 7.4
    TABLES_PER_VERSION.put(2329, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures", "project_measures",
      "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics", "issues", "issue_changes",
      "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources", "ce_queue", "ce_activity",
      "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "ce_task_message", "user_tokens", "es_queue", "plugins", "project_branches", "analysis_properties",
      "webhooks", "webhook_deliveries", "alm_app_installs", "project_alm_bindings", "project_mappings"));

    // SonarQube 7.5
    TABLES_PER_VERSION.put(2404, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "event_component_changes", "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures",
      "project_measures", "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics",
      "issues", "issue_changes", "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources",
      "ce_queue", "ce_activity", "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "ce_task_message", "user_tokens", "es_queue", "plugins", "project_branches",
      "analysis_properties", "webhooks", "webhook_deliveries", "alm_app_installs", "project_alm_bindings", "project_mappings", "organization_alm_bindings"));
    // SonarQube 7.6
    TABLES_PER_VERSION.put(2508, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "event_component_changes", "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures",
      "project_measures", "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics",
      "issues", "issue_changes", "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources",
      "ce_queue", "ce_activity", "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "ce_task_message", "user_tokens", "es_queue", "plugins", "project_branches",
      "analysis_properties", "webhooks", "webhook_deliveries", "alm_app_installs", "project_alm_bindings", "project_mappings", "organization_alm_bindings", "user_properties"));

    // SonarQube 7.7
    TABLES_PER_VERSION.put(2612, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "event_component_changes", "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures",
      "project_measures", "internal_properties", "projects", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters", "users", "metrics",
      "issues", "issue_changes", "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes", "file_sources",
      "ce_queue", "ce_activity", "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "ce_task_message", "user_tokens", "es_queue", "plugins", "project_branches",
      "analysis_properties", "webhooks", "webhook_deliveries", "alm_app_installs", "project_alm_bindings", "project_mappings", "organization_alm_bindings", "user_properties"));

    // SonarQube 7.8
    TABLES_PER_VERSION.put(2708, Arrays.asList(
      "organizations", "organization_members", "groups_users", "rules_parameters", "rules_profiles", "org_qprofiles", "default_qprofiles", "project_qprofiles",
      "qprofile_edit_users", "qprofile_edit_groups", "groups", "snapshots", "group_roles", "rule_repositories", "deprecated_rule_keys", "rules", "rules_metadata", "events",
      "event_component_changes", "quality_gates", "quality_gate_conditions", "org_quality_gates", "properties", "project_links", "duplications_index", "live_measures",
      "project_measures", "internal_properties", "projects", "internal_component_props", "manual_measures", "active_rules", "notifications", "user_roles", "active_rule_parameters",
      "users", "metrics", "issues", "issue_changes", "permission_templates", "perm_tpl_characteristics", "perm_templates_users", "perm_templates_groups", "qprofile_changes",
      "file_sources", "ce_queue", "ce_activity", "ce_task_characteristics", "ce_task_input", "ce_scanner_context", "ce_task_message", "user_tokens", "es_queue", "plugins",
      "project_branches", "analysis_properties", "webhooks", "webhook_deliveries", "alm_app_installs", "project_alm_bindings", "project_mappings", "organization_alm_bindings",
      "user_properties"));
  }
}
