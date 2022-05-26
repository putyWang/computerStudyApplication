package com.learning.commons.config;

import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Comparator;
import java.util.List;

public class SwaggerPluginRegistry extends OrderAwarePluginRegistry<DocumentationPlugin, DocumentationType> implements PluginRegistry<DocumentationPlugin, DocumentationType> {
    protected SwaggerPluginRegistry(List<Docket> plugins, Comparator<? super DocumentationPlugin> comparator) {
        super(plugins, comparator);
    }

    public List<DocumentationPlugin> getPlugins() {
        return super.getPlugins();
    }
}
