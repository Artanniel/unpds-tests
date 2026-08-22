package com.artantech.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class RepositoryAccessRulesTest {

    private JavaClasses javaClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.artantech.paymentservice");

    @Test
    void controllersTest() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..repository..");

        rule.check(javaClasses);
    }

    @Test
    void repositoriesTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().resideInAPackage("..repository..")
                .should().beInterfaces();

        rule.check(javaClasses);
    }
}
