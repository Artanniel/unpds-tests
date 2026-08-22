package com.artantech.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;

class PersistenceBoundariesRulesTest {

    private JavaClasses javaClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.artantech.paymentservice");

    @Test
    void entitiesTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().areAnnotatedWith(Entity.class)
                .should().resideInAPackage("..model..");

        rule.check(javaClasses);
    }

    @Test
    void dtoTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAnyPackage("..dto..", "..controller..")
                .should().notBeAnnotatedWith(Entity.class);

        rule.check(javaClasses);
    }
}
