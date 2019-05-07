package com.blueriq;

import com.aquima.plugin.instanceselectorplus.config.InstanceSelectorPlusConfig;
import com.aquima.web.boot.RootConfig;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class WarApplication extends AbstractWarApplication {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return RootConfig.configure(application).sources(InstanceSelectorPlusConfig.class);
  }
}
