package com.learning.swagger.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.learning.core.annotion.AccessKey;
import com.learning.core.annotion.ApiVersion;
import com.learning.core.annotion.IgnoreAuth;
import com.learning.core.utils.ArrayUtils;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.DynamicEnumUtil;
import com.learning.core.utils.ListUtils;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.PatternMatchUtils;
import springfox.documentation.RequestHandler;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.DocumentationPlugin;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Configuration
@Primary
@EnableOpenApi
@EnableKnife4j
@EnableConfigurationProperties({SwaggerProperties.class})
@ComponentScan({"com.learning.core"})
@ConditionalOnProperty(
        value = {"knife4j.enable"},
        matchIfMissing = true
)
public class Swagger3Config extends DocumentationPluginsManager {
    private static final Logger log = LoggerFactory.getLogger(Swagger3Config.class);
    @Value("${spring.application.name: Default}")
    private String applicationName;
    private static final String SPLIT_COMMA = ",";
    private static final String SPLIT_SEMICOLON = ";";
    private Class<?>[] ignoredParameterTypes = new Class[]{ServletRequest.class, ServletResponse.class, HttpServletRequest.class, HttpServletResponse.class, HttpSession.class, ApiIgnore.class};
    private static final List<String> excludedPathPrefix = Arrays.asList("/gen/");
    @Resource
    private SwaggerProperties swaggerProperties;
    private final OpenApiExtensionResolver openApiExtensionResolver;

    @Autowired
    public Swagger3Config(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    public Collection<DocumentationPlugin> documentationPlugins()
            throws IllegalStateException {
        List<DocumentationPlugin> plugins = this.registry().getPlugins();
        this.ensureNoDuplicateGroups(plugins);
        return plugins.isEmpty() ? Collections.singleton(this.defaultDocumentationPlugin()) : plugins;
    }

    private void ensureNoDuplicateGroups(List<DocumentationPlugin> allPlugins) throws IllegalStateException {
        Map<String, List<DocumentationPlugin>> plugins = allPlugins.stream().collect(Collectors.groupingBy((input) -> {
            return Optional.ofNullable(input.getGroupName()).orElse("default");
        }, LinkedHashMap::new, Collectors.toList()));
        Iterable<String> duplicateGroups = plugins.entrySet().stream().filter((input) -> {
            return (input.getValue()).size() > 1;
        }).map(Map.Entry::getKey).collect(Collectors.toList());
        if (StreamSupport.stream(duplicateGroups.spliterator(), false).count() > 0L) {
            throw new IllegalStateException(String.format("Multiple Dockets with the same group name are not supported. The following duplicate groups were discovered. %s", String.join(",", duplicateGroups)));
        }
    }

    private DocumentationPlugin defaultDocumentationPlugin() {
        return new Docket(DocumentationType.OAS_30);
    }

    private SwaggerPluginRegistry registry() {
        List<Docket> list = new ArrayList<>();
        String[] basePackages = this.getBasePackages();
        this.dynamicGroupEnum();
        ApiVersion.Group[] groups = ApiVersion.Group.values();

        for(int i = 0; i < groups.length; i ++) {
            ApiVersion.Group version = groups[i];
            Docket docket = (new Docket(DocumentationType.OAS_30))
                    .apiInfo(this.apiInfo()).groupName(version.getDisplay())
                    .select()
                    .apis((input) -> {
                boolean isApiAnnotation = input.isAnnotatedWith(Api.class);

                if (isApiAnnotation) {
                    return ApiVersion.Group.DEFAULT.equals(version);
                } else {
                    boolean isApiVersionAnnotation = input.isAnnotatedWith(ApiVersion.class);
                    return isApiVersionAnnotation ? input
                            .findControllerAnnotation(ApiVersion.class)
                            .filter((item) -> {
                        return Arrays.asList(item.value()).contains(version);
                    }).isPresent() : declaringClass(input).filter((f) -> {
                        Api apiAnnotation = f.getPackage().getAnnotation(Api.class);

                        if (apiAnnotation == null) {

                            for (String strPackage : basePackages) {
                                boolean isMatch = f.getPackage().getName().startsWith(strPackage);
                                if (isMatch) {
                                    return true;
                                }

                                isMatch = PatternMatchUtils.simpleMatch(strPackage, f.getPackage().getName());
                                if (isMatch) {
                                    return true;
                                }
                            }
                        }

                        return false;
                    }).isPresent();
                }
            }).paths((s) -> {
                Iterator<String> it = excludedPathPrefix.iterator();

                String pathPrefix;
                do {
                    if (! it.hasNext()) {
                        return true;
                    }

                    pathPrefix = it.next();
                } while(!StringUtils.startsWith(s, pathPrefix));

                return false;
            }).build()
                    .extensions(this.openApiExtensionResolver.buildSettingExtensions())
                    .protocols(Stream.of("https", "http").collect(Collectors.toSet()))
                    .ignoredParameterTypes(this.ignoredParameterTypes)
                    .globalRequestParameters(this.getParameters())
                    .securitySchemes(this.securitySchemes())
                    .securityContexts(this.securityContexts());

            list.add(docket);
        }

        return new SwaggerPluginRegistry(list, new AnnotationAwareOrderComparator());
    }

    public String[] getBasePackages() {
        log.debug("swaggerProperties = " + this.swaggerProperties);
        String basePackage = this.swaggerProperties.getBasePackage();
        if (StringUtils.isBlank(basePackage)) {
            basePackage = "";
        }

        String[] basePackages = null;
        if (basePackage.contains(",")) {
            basePackages = basePackage.split(",");
        } else if (basePackage.contains(";")) {
            basePackages = basePackage.split(";");
        } else {
            basePackages = new String[]{basePackage};
        }

        List<String> defaultPackage = new ArrayList<>();
        defaultPackage.add("com.learning.**.controller");
        defaultPackage.add("com.learning.modules.**.controller");
        if (ArrayUtils.isNotEmpty(basePackages)) {
            defaultPackage.addAll(Arrays.asList(basePackages));
        }

        List<String> distinctPackage = ListUtils.distinct(defaultPackage);
        log.info("swagger scan basePackages:" + Arrays.toString(distinctPackage.toArray(new String[0])));
        return CollectionUtils.isEmpty(distinctPackage) ? new String[0] : (String[])distinctPackage.toArray(new String[0]);
    }

    private List<RequestParameter> getParameters() {
        List<SwaggerProperties.ParameterConfig> parameterConfig = this.swaggerProperties.getParameterConfig();
        if (CollectionUtils.isEmpty(parameterConfig)) {
            return null;
        } else {
            List<RequestParameter> parameters = new ArrayList<>();
            parameterConfig.forEach((parameter) -> {
                parameters.add((new RequestParameterBuilder()).name(parameter.getName()).description(parameter.getDescription()).in(ParameterType.QUERY).query((q) -> {
                    q.model((m) -> {
                        m.scalarModel(ScalarType.STRING);
                    });
                }).required(parameter.isRequired()).build());
            });
            return parameters;
        }
    }

    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        securitySchemes.add(new ApiKey("Authorization", "token", In.HEADER.toValue()));
        securitySchemes.add(new ApiKey("accessKey", "accessKey", In.QUERY.toValue()));
        return securitySchemes;
    }

    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(SecurityContext.builder().securityReferences(Collections.singletonList(SecurityReference.builder().scopes(new AuthorizationScope[0]).reference("Authorization").build())).operationSelector((o) -> {
            return !o.findAnnotation(IgnoreAuth.class).isPresent();
        }).build());
        securityContexts.add(SecurityContext.builder().securityReferences(Collections.singletonList(SecurityReference.builder().scopes(new AuthorizationScope[0]).reference("accessKey").build())).operationSelector((o) -> {
            return !o.findAnnotation(AccessKey.class).isPresent();
        }).build());

        return securityContexts;
    }

    private ApiInfo apiInfo() {
        return (new ApiInfoBuilder())
                .title(this.applicationName)
                .description(this.applicationName + "API Documents")
                .termsOfServiceUrl(this.swaggerProperties.getUrl())
                .contact(
                        new Contact(this.swaggerProperties.getContactName(),
                        this.swaggerProperties.getContactUrl(),
                        this.swaggerProperties.getContactEmail())
                ).version(this.swaggerProperties.getVersion()).build();
    }

    private void dynamicGroupEnum() {
        List<SwaggerProperties.Group> groupList = this.swaggerProperties.getGroup();
        Class var2 = SwaggerProperties.Group.class;
        synchronized(SwaggerProperties.Group.class) {
            Iterator<SwaggerProperties.Group> it = groupList.iterator();

            while(it.hasNext()) {
                SwaggerProperties.Group group = it.next();
                ApiVersion.Group[] groups = ApiVersion.Group.values();

                for(ApiVersion.Group g : groups) {

                    if (!g.getDisplay().equals(group.getGroupDisplay())) {
                        DynamicEnumUtil.addEnum(ApiVersion.Group.class, group.getEnumName(), new Class[]{String.class}, new Object[]{group.getGroupDisplay()});
                    }
                }
            }

        }
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.ofNullable(input.declaringClass());
    }

    private List<Response> getGlobalResponseMessage() {
        List<Response> responseList = new ArrayList<>();
        responseList.add((new ResponseBuilder()).code("404").description("找不到资源").build());
        return responseList;
    }
}
