package com.artantech.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeneralRulesTest {

    private JavaClasses javaClasses =
            new ClassFileImporter()
                    .withImportOption(new ImportOption.DoNotIncludeTests())
                    .importPackages("com.artantech.paymentservice");

    @Test
    @DisplayName("Controller should not access repository")
    void controllerTest() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(javaClasses);
    }

    @Test
    void repositoryTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..repository..")
                .should().beInterfaces();

        rule.check(javaClasses);
    }

    @Test
    void entityTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().areAnnotatedWith(Entity.class)
                .should().resideInAnyPackage("..model..");

        rule.check(javaClasses);
    }

    @Test
    void shouldNotUseGenericExceptions() {
        ArchRule rule = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
        rule.check(javaClasses);
    }

    @Test
    void shouldNotUseDeprecatedClasses() {
        ArchRule rule = GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;
        rule.check(javaClasses);
    }
}
