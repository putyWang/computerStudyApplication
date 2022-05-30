package com.learning.swagger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(
        prefix = "swagger"
)
public class SwaggerProperties {
    private boolean enable = false;
    @Value("${swagger.base.package:#{null}}")
    private String basePackage;
    @Value("${swagger.contact.email:common}")
    private String contactEmail;
    @Value("${swagger.contact.name: 13540025344@163.com}")
    private String contactName;
    @Value("${swagger.contact.url: common}")
    private String contactUrl;
    private String description = "";
    private String title = "common";
    private String url = "";
    private String version = "1.0.0";
    @NestedConfigurationProperty
    private List<ParameterConfig> parameterConfig = new ArrayList<>();
    @NestedConfigurationProperty
    private List<SwaggerProperties.Group> group = new ArrayList<>();

    public SwaggerProperties() {
    }

    public boolean isEnable() {
        return this.enable;
    }

    public String getBasePackage() {
        return this.basePackage;
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getContactUrl() {
        return this.contactUrl;
    }

    public String getDescription() {
        return this.description;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public String getVersion() {
        return this.version;
    }

    public List<SwaggerProperties.ParameterConfig> getParameterConfig() {
        return this.parameterConfig;
    }

    public List<SwaggerProperties.Group> getGroup() {
        return this.group;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setParameterConfig(List<SwaggerProperties.ParameterConfig> parameterConfig) {
        this.parameterConfig = parameterConfig;
    }

    public void setGroup(List<SwaggerProperties.Group> group) {
        this.group = group;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof SwaggerProperties)) {
            return false;
        } else {
            SwaggerProperties other = (SwaggerProperties)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.isEnable() != other.isEnable()) {
                return false;
            } else {
                Object this$basePackage = this.getBasePackage();
                Object other$basePackage = other.getBasePackage();
                if (this$basePackage == null) {
                    if (other$basePackage != null) {
                        return false;
                    }
                } else if (!this$basePackage.equals(other$basePackage)) {
                    return false;
                }

                Object this$contactEmail = this.getContactEmail();
                Object other$contactEmail = other.getContactEmail();
                if (this$contactEmail == null) {
                    if (other$contactEmail != null) {
                        return false;
                    }
                } else if (!this$contactEmail.equals(other$contactEmail)) {
                    return false;
                }

                label119: {
                    Object this$contactName = this.getContactName();
                    Object other$contactName = other.getContactName();
                    if (this$contactName == null) {
                        if (other$contactName == null) {
                            break label119;
                        }
                    } else if (this$contactName.equals(other$contactName)) {
                        break label119;
                    }

                    return false;
                }

                label112: {
                    Object this$contactUrl = this.getContactUrl();
                    Object other$contactUrl = other.getContactUrl();
                    if (this$contactUrl == null) {
                        if (other$contactUrl == null) {
                            break label112;
                        }
                    } else if (this$contactUrl.equals(other$contactUrl)) {
                        break label112;
                    }

                    return false;
                }

                Object this$description = this.getDescription();
                Object other$description = other.getDescription();
                if (this$description == null) {
                    if (other$description != null) {
                        return false;
                    }
                } else if (!this$description.equals(other$description)) {
                    return false;
                }

                Object this$title = this.getTitle();
                Object other$title = other.getTitle();
                if (this$title == null) {
                    if (other$title != null) {
                        return false;
                    }
                } else if (!this$title.equals(other$title)) {
                    return false;
                }

                label91: {
                    Object this$url = this.getUrl();
                    Object other$url = other.getUrl();
                    if (this$url == null) {
                        if (other$url == null) {
                            break label91;
                        }
                    } else if (this$url.equals(other$url)) {
                        break label91;
                    }

                    return false;
                }

                Object this$version = this.getVersion();
                Object other$version = other.getVersion();
                if (this$version == null) {
                    if (other$version != null) {
                        return false;
                    }
                } else if (!this$version.equals(other$version)) {
                    return false;
                }

                Object this$parameterConfig = this.getParameterConfig();
                Object other$parameterConfig = other.getParameterConfig();
                if (this$parameterConfig == null) {
                    if (other$parameterConfig != null) {
                        return false;
                    }
                } else if (!this$parameterConfig.equals(other$parameterConfig)) {
                    return false;
                }

                Object this$group = this.getGroup();
                Object other$group = other.getGroup();
                if (this$group == null) {
                    if (other$group != null) {
                        return false;
                    }
                } else if (!this$group.equals(other$group)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof SwaggerProperties;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.isEnable() ? 79 : 97);
        Object $basePackage = this.getBasePackage();
        result = result * 59 + ($basePackage == null ? 43 : $basePackage.hashCode());
        Object $contactEmail = this.getContactEmail();
        result = result * 59 + ($contactEmail == null ? 43 : $contactEmail.hashCode());
        Object $contactName = this.getContactName();
        result = result * 59 + ($contactName == null ? 43 : $contactName.hashCode());
        Object $contactUrl = this.getContactUrl();
        result = result * 59 + ($contactUrl == null ? 43 : $contactUrl.hashCode());
        Object $description = this.getDescription();
        result = result * 59 + ($description == null ? 43 : $description.hashCode());
        Object $title = this.getTitle();
        result = result * 59 + ($title == null ? 43 : $title.hashCode());
        Object $url = this.getUrl();
        result = result * 59 + ($url == null ? 43 : $url.hashCode());
        Object $version = this.getVersion();
        result = result * 59 + ($version == null ? 43 : $version.hashCode());
        Object $parameterConfig = this.getParameterConfig();
        result = result * 59 + ($parameterConfig == null ? 43 : $parameterConfig.hashCode());
        Object $group = this.getGroup();
        result = result * 59 + ($group == null ? 43 : $group.hashCode());
        return result;
    }

    public String toString() {
        return "SwaggerProperties(enable=" + this.isEnable() + ", basePackage=" + this.getBasePackage() + ", contactEmail=" + this.getContactEmail() + ", contactName=" + this.getContactName() + ", contactUrl=" + this.getContactUrl() + ", description=" + this.getDescription() + ", title=" + this.getTitle() + ", url=" + this.getUrl() + ", version=" + this.getVersion() + ", parameterConfig=" + this.getParameterConfig() + ", group=" + this.getGroup() + ")";
    }

    public static class Group {
        private String enumName;
        private String groupDisplay;

        public Group() {
        }

        public Group(String enumName, String groupDisplay) {
            this.enumName = enumName;
            this.groupDisplay = groupDisplay;
        }

        public String getEnumName() {
            return this.enumName;
        }

        public String getGroupDisplay() {
            return this.groupDisplay;
        }

        public void setEnumName(String enumName) {
            this.enumName = enumName;
        }

        public void setGroupDisplay(String groupDisplay) {
            this.groupDisplay = groupDisplay;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof SwaggerProperties.Group)) {
                return false;
            } else {
                SwaggerProperties.Group other = (SwaggerProperties.Group)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    Object this$enumName = this.getEnumName();
                    Object other$enumName = other.getEnumName();
                    if (this$enumName == null) {
                        if (other$enumName != null) {
                            return false;
                        }
                    } else if (!this$enumName.equals(other$enumName)) {
                        return false;
                    }

                    Object this$groupDisplay = this.getGroupDisplay();
                    Object other$groupDisplay = other.getGroupDisplay();
                    if (this$groupDisplay == null) {
                        if (other$groupDisplay != null) {
                            return false;
                        }
                    } else if (!this$groupDisplay.equals(other$groupDisplay)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof SwaggerProperties.Group;
        }

        public int hashCode() {
            int result = 1;
            Object $enumName = this.getEnumName();
            result = result * 59 + ($enumName == null ? 43 : $enumName.hashCode());
            Object $groupDisplay = this.getGroupDisplay();
            result = result * 59 + ($groupDisplay == null ? 43 : $groupDisplay.hashCode());
            return result;
        }

        public String toString() {
            return "SwaggerProperties.Group(enumName=" + this.getEnumName() + ", groupDisplay=" + this.getGroupDisplay() + ")";
        }
    }

    public static class ParameterConfig {
        private String name = "Authorization";
        private String description = "Token Request Header";
        private String type = "head";
        private String dataType = "String";
        private boolean required = false;
        private String defaultValue;

        public ParameterConfig() {
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public String getType() {
            return this.type;
        }

        public String getDataType() {
            return this.dataType;
        }

        public boolean isRequired() {
            return this.required;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof SwaggerProperties.ParameterConfig)) {
                return false;
            } else {
                SwaggerProperties.ParameterConfig other = (SwaggerProperties.ParameterConfig)o;
                if (!other.canEqual(this)) {
                    return false;
                } else if (this.isRequired() != other.isRequired()) {
                    return false;
                } else {
                    label73: {
                        Object this$name = this.getName();
                        Object other$name = other.getName();
                        if (this$name == null) {
                            if (other$name == null) {
                                break label73;
                            }
                        } else if (this$name.equals(other$name)) {
                            break label73;
                        }

                        return false;
                    }

                    Object this$description = this.getDescription();
                    Object other$description = other.getDescription();
                    if (this$description == null) {
                        if (other$description != null) {
                            return false;
                        }
                    } else if (!this$description.equals(other$description)) {
                        return false;
                    }

                    label59: {
                        Object this$type = this.getType();
                        Object other$type = other.getType();
                        if (this$type == null) {
                            if (other$type == null) {
                                break label59;
                            }
                        } else if (this$type.equals(other$type)) {
                            break label59;
                        }

                        return false;
                    }

                    Object this$dataType = this.getDataType();
                    Object other$dataType = other.getDataType();
                    if (this$dataType == null) {
                        if (other$dataType != null) {
                            return false;
                        }
                    } else if (!this$dataType.equals(other$dataType)) {
                        return false;
                    }

                    Object this$defaultValue = this.getDefaultValue();
                    Object other$defaultValue = other.getDefaultValue();
                    if (this$defaultValue == null) {
                        if (other$defaultValue != null) {
                            return false;
                        }
                    } else if (!this$defaultValue.equals(other$defaultValue)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof SwaggerProperties.ParameterConfig;
        }

        public int hashCode() {
            int result = 1;
            result = result * 59 + (this.isRequired() ? 79 : 97);
            Object $name = this.getName();
            result = result * 59 + ($name == null ? 43 : $name.hashCode());
            Object $description = this.getDescription();
            result = result * 59 + ($description == null ? 43 : $description.hashCode());
            Object $type = this.getType();
            result = result * 59 + ($type == null ? 43 : $type.hashCode());
            Object $dataType = this.getDataType();
            result = result * 59 + ($dataType == null ? 43 : $dataType.hashCode());
            Object $defaultValue = this.getDefaultValue();
            result = result * 59 + ($defaultValue == null ? 43 : $defaultValue.hashCode());
            return result;
        }

        public String toString() {
            return "SwaggerProperties.ParameterConfig(name=" + this.getName() + ", description=" + this.getDescription() + ", type=" + this.getType() + ", dataType=" + this.getDataType() + ", required=" + this.isRequired() + ", defaultValue=" + this.getDefaultValue() + ")";
        }
    }
}
