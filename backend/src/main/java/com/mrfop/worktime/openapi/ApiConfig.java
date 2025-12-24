package com.mrfop.worktime.openapi;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class ApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI().info(new Info()
                .title("Worktime API")
                .description("Work time tracking")
                .version("v1"));
    }

    @Bean
    public OpenApiCustomizer problemResponsesCustomizer() {
        return openApi -> {
            ensureComponents(openApi);
            Components components = openApi.getComponents();

            registerSchema(components, ApiProblem.class);
            registerSchema(components, ApiProblem.FieldViolation.class);

            components.addResponses("BadRequestProblem",
                    new ApiResponse().description("Bad Request").content(problemContent()));
            components.addResponses("NotFoundProblem",
                    new ApiResponse().description("Not Found").content(problemContent()));
            components.addResponses("ConflictProblem",
                    new ApiResponse().description("Conflict").content(problemContent()));
            components.addResponses("InternalErrorProblem",
                    new ApiResponse().description("Internal Server Error").content(problemContent()));
        };
    }

    private void ensureComponents(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }
    }

    private Content problemContent() {
        MediaType media = new MediaType();
        media.setSchema(new Schema<>().$ref("#/components/schemas/ApiProblem"));

        Content content = new Content();
        content.addMediaType("application/problem+json", media);
        content.addMediaType("application/json", media);
        return content;
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerSchema(Components components, Class<?> type) {
        Map<String, Schema> schemas = (Map) ModelConverters.getInstance().read(type);
        schemas.forEach((name, schema) -> {
            if (components.getSchemas() == null || !components.getSchemas().containsKey(name)) {
                components.addSchemas(name, schema);
            }
        });
    }
}
