package com.artantech.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

class StereotypeAndNamingRulesTest {

    private JavaClasses javaClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.artantech.paymentservice");

    @Test
    void controllersTest() {
        ArchRule rule = ArchRuleDefinition.classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..controller..")
                .andShould().haveSimpleNameEndingWith("Controller");

        rule.check(javaClasses);
    }
}
