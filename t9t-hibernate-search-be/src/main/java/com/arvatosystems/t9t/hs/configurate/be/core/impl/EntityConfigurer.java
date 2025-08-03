package com.arvatosystems.t9t.hs.configurate.be.core.impl;

import com.arvatosystems.t9t.hs.configurate.be.core.model.EmbeddedIndexEntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.model.EntitySearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.model.FieldConfig;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;
import de.jpaw.dp.Singleton;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmMappingConfigurationContext;
import org.hibernate.search.mapper.orm.mapping.HibernateOrmSearchMappingConfigurer;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.PropertyMappingStep;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EntityConfigurer implements HibernateOrmSearchMappingConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConfigurer.class);

    /**
     * This method configures the Hibernate Search mapping for entity classes.
     * It loads configuration from a JSON file and applies it programmatically.
     * EntityConfigurer.configure is called automatically by Hibernate Search during initialization
     * (see: com.arvatosystems.t9t.orm.jpa.hibernate.impl.EMFCustomizer).
     */
    @Override
    public void configure(HibernateOrmMappingConfigurationContext context) {

        LOGGER.info("Starting Hibernate Search entity configuration");

        // Load configuration
        EntitySearchConfiguration configuration = ConfigurationLoader.loadConfiguration();
        if (configuration == null || configuration.getEntities() == null) {
            LOGGER.warn("No configuration found, using fallback configuration");
            return;
        }

        ProgrammaticMappingConfigurationContext mapping = context.programmaticMapping();

        // Configure each entity from the configuration file
        for (EntityConfig entityConfig : configuration.getEntities()) {
            configureEntity(mapping, entityConfig);
        }

        LOGGER.info("Completed Hibernate Search entity configuration for {} entities",
                configuration.getEntities().size());

        // Note: Index summary logging moved to separate service to avoid EMF dependency during configuration
    }

    /**
     * Configures a single entity based on the provided configuration.
     */
    private void configureEntity(ProgrammaticMappingConfigurationContext mapping, EntityConfig config) {

        LOGGER.debug("Configuring entity: {}", config.getClassName());

        try {
            // Create type mapping for the entity class
            TypeMappingStep typeMapping = mapping.type(config.getClassName());

            // Apply @Indexed annotation if configured
            typeMapping.indexed();
            LOGGER.debug("Applied @Indexed to entity: {}", config.getClassName());

            // Configure fields
            if (config.getFields() != null) {
                for (FieldConfig fieldConfig : config.getFields()) {
                    configureField(typeMapping, fieldConfig);
                }
            }

            // Configure embedded fields
            if (config.getEmbeddedIndexEntities() != null) {
                for (EmbeddedIndexEntityConfig embeddedConfig : config.getEmbeddedIndexEntities()) {
                    configureEmbeddedIndexEntity(typeMapping, embeddedConfig);
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to configure entity {}: {}", config.getClassName(), e.getMessage(), e);
        }
    }

    /**
     * Configures a field based on the field configuration.
     */
    private void configureField(TypeMappingStep typeMapping, FieldConfig config) {

        LOGGER.debug("Configuring field: {} with type: {}", config.getName(), config.getType());
        PropertyMappingStep propertyStep = typeMapping.property(config.getName());

        switch (config.getType().toLowerCase()) {
            case "fulltextfield":
                if (config.getAnalyzer() != null) {
                    propertyStep.fullTextField().analyzer(config.getAnalyzer());
                } else {
                    propertyStep.fullTextField();
                }
                break;
            case "keywordfield":
                propertyStep.keywordField();
                break;
            case "genericfield":
                propertyStep.genericField();
                break;
            default:
                LOGGER.warn("Unknown field type '{}' for field '{}', using fullTextField as default",
                        config.getType(), config.getName());
                propertyStep.fullTextField();
        }
    }

    /**
     * Configures an embedded field based on the embedded field configuration.
     */
    private void configureEmbeddedIndexEntity(TypeMappingStep typeMapping, EmbeddedIndexEntityConfig config) {

        LOGGER.debug("Configuring embedded field: {}", config.getTargetEntity());
        typeMapping.property(config.getTargetEntity())
                .indexedEmbedded()
                .indexingDependency().reindexOnUpdate(ReindexOnUpdate.SHALLOW);
    }
}
