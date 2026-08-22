package com.artantech.paymentservice.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.Test;

class HygieneRulesTest {

    private JavaClasses javaClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.artantech.paymentservice");

    @Test
    void noGenericExceptionsShouldBeThrown() {
        ArchRule rule = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
        rule.check(javaClasses);
    }

    @Test
    void shouldNotUseDeprecatedApi() {
        ArchRule rule = GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;
        rule.check(javaClasses);
    }
}
