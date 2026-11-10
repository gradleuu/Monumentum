package io.max.monumentum.config;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Nest;

@Config(name = "monumentum", wrapperName = "MonumentumConfig")
public final class MonumentumConfigModel {
  @Nest public ApiOptions api = new ApiOptions();
  @Nest public StrikeChestsOptions strike_chests = new StrikeChestsOptions();
  @Nest public CrimsonContractSwitchReminderOptions crimson_contract_switch_reminder =
          new CrimsonContractSwitchReminderOptions();
}