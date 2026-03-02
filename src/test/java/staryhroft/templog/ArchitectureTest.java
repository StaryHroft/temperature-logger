package staryhroft.templog;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.base.DescribedPredicate;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "staryhroft.templog")
public class ArchitectureTest {

    // ========== 1. ПРОВЕРКА ИМЕНОВАНИЯ ==========
    @ArchTest
    static final ArchRule controllers_should_be_suffixed = classes()
            .that().resideInAPackage("..controller..")
            .and().haveSimpleNameNotEndingWith("Test")  // исключаем тесты
            .should().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule services_should_be_suffixed = classes()
            .that().resideInAPackage("..service..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().haveSimpleNameEndingWith("Service")
            .orShould().haveSimpleNameEndingWith("Manager")
            .orShould().haveSimpleNameEndingWith("Finder")
            .orShould().haveSimpleNameEndingWith("Updater");

    @ArchTest
    static final ArchRule repositories_should_be_suffixed = classes()
            .that().resideInAPackage("..repository..")
            .should().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule entities_should_reside_in_entity_package = classes()
            .that().haveSimpleName("City")
            .or().haveSimpleName("CityTemperature")
            .or().haveSimpleName("User")
            .should().resideInAPackage("..entity..");

    @ArchTest
    static final ArchRule dtos_should_be_suffixed = classes()
            .that().resideInAPackage("..dto..")
            .and().areTopLevelClasses()   // только top-level классы
            .should().haveSimpleNameEndingWith("Dto")
            .orShould().haveSimpleNameEndingWith("Response")
            .orShould().haveSimpleNameEndingWith("Request");

    @ArchTest
    static final ArchRule exceptions_should_be_suffixed = classes()
            .that().resideInAPackage("..exception..")
            .and().resideOutsideOfPackage("..exception.message..") // не проверяем message
            .should().haveSimpleNameEndingWith("Exception");

    // ========== 2. ПРОВЕРКА СЛОЁВ ==========
    @ArchTest
    static final ArchRule layer_dependencies = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Repository").definedBy("..repository..")
            .layer("Integration").definedBy("..client..")
            .whereLayer("Controller").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Integration")
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
            .whereLayer("Integration").mayOnlyBeAccessedByLayers("Service")
            // Игнорируем конкретные классы через предикаты по имени
            .ignoreDependency(
                    DescribedPredicate.describe("DataInitializer",
                            javaClass -> javaClass.getName().equals("staryhroft.templog.config.DataInitializer")),
                    DescribedPredicate.alwaysTrue()
            )
            .ignoreDependency(
                    DescribedPredicate.describe("WeatherUpdateScheduler",
                            javaClass -> javaClass.getName().equals("staryhroft.templog.scheduler.WeatherUpdateScheduler")),
                    DescribedPredicate.alwaysTrue()
            );
    // ========== 3. ЗАПРЕЩЁННЫЕ ЗАВИСИМОСТИ ==========
    @ArchTest
    static final ArchRule controllers_should_not_access_repositories_directly = noClasses()
            .that().resideInAPackage("..controller..")
            .should().accessClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule services_should_not_access_controllers = noClasses()
            .that().resideInAPackage("..service..")
            .should().accessClassesThat().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule controllers_should_not_use_entities_directly = noClasses()
            .that().resideInAPackage("..controller..")
            .and().haveSimpleNameNotEndingWith("Test")
            .should().accessClassesThat().resideInAPackage("..entity..");

    // ========== 4. ОТСУТСТВИЕ ЦИКЛОВ ==========
    @ArchTest
    static final ArchRule no_cycles_in_packages = slices()
            .matching("staryhroft.templog.(*)..")
            .should().beFreeOfCycles();
}
