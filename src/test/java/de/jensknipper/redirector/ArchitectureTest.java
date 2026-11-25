package de.jensknipper.redirector;

import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.core.ClassCriteria;
import com.enofex.taikai.java.ImportPatterns;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ArchitectureTest {

  @Test
  void architecture() {
    Taikai.builder()
        .namespace("de.jensknipper.redirector")
        .excludeClasses("de.jensknipper.redirector.database..")
        // .failOnEmpty(true) no utility class
        .java(java ->
            java.classesShouldBeNullMarked()
                .fieldsShouldNotBePublic()
                .imports(imports ->
                    imports
                        .shouldHaveNoCycles()
                        .shouldNotImport(ImportPatterns.lombok())
                        .shouldNotImport(ImportPatterns.hamcrest())
                        .shouldNotImport(ImportPatterns.shaded()))
                .naming(naming ->
                    naming
                        .classesShouldNotMatch(".*Impl")
                        .constantsShouldFollowConventions()
                        .enumConstantsShouldFollowConventions()
                        .interfacesShouldNotHavePrefixI()
                        .packagesShouldMatchDefault())
                .noUsageOfDeprecatedAPIs()
                .noUsageOfSystemOutOrErr()
                .methodsShouldNotDeclareGenericExceptions()
                .utilityClassesShouldBeFinalAndHavePrivateConstructor()
                .serialVersionUIDFieldsShouldBeStaticFinalLong())
        .logging(logging ->
            logging.loggersShouldFollowConventions(Logger.class, "LOG", List.of(PRIVATE, FINAL)))
        .spring(spring ->
            spring
                .boot(boot -> boot.applicationClassShouldResideInPackage())
                .configurations(
                    configurations -> configurations.namesShouldMatch(".*(Config|Configuration)"))
                .controllers(controllers ->
                    controllers
                        .namesShouldEndWithController()
                        .shouldBePackagePrivate()
                        .shouldNotDependOnOtherControllers()
                        .shouldNotDependOnRepositories()
                        .shouldBeAnnotatedWithValidated()
                        .requestBodiesShouldBeValidated())
                .noAutowiredFields()
                .noInjectionIntoStaticFields()
                .noSelfInvocationOfProxiedMethods()
                .properties(properties ->
                    properties
                        .namesShouldEndWithProperties()
                        .shouldBeAnnotatedWithConfigurationProperties()
                        .shouldBeRecords())
                .repositories(repositories ->
                    repositories
                        .namesShouldEndWithRepository()
                        .shouldBeAnnotatedWithRepository()
                        .shouldNotDependOnServices()
                        .shouldNotDependOnControllers())
                .services(services ->
                    services
                        .namesShouldEndWithService()
                        .shouldBeAnnotatedWithService()
                        .shouldNotDependOnControllers())
                .transactional(transactional ->
                    transactional
                        .methodsShouldBePublic()
                        .shouldNotBeSelfInvoked()
                        .shouldNotBeUsedInControllers()))
        .test(test ->
            test.junit(junit ->
                junit
                    .classesShouldMatch(".*(Test|IT)")
                    .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*(Test|IT)"))
                    .classesShouldNotBeAnnotatedWithDisabled()
                    .methodsShouldBePackagePrivate()
                    // .methodsShouldContainAssertionsOrVerifications()
                    // wait for playwright assertions to be integrated
                    .methodsShouldNotBeAnnotatedWithDisabled()
                    .nestedClassesShouldBeAnnotatedWithNested()
                    .testsShouldBeExecutable()))
        .build()
        .checkAll();
  }
}
