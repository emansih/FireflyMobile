package xyz.hisname.fireflyiii.repository.models.rules

data class Relationships(
        val rule_group: RuleGroup,
        val rule_triggers: RuleTriggers,
        val rule_actions: RuleActions
)