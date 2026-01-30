#noinspection CucumberUndefinedStep
@cucumber @bakery
Feature: Bakery plugin tests

  Scenario: Canary
    Given a new Bakery project
    When I am executing the task 'tasks'
    Then the build should succeed

  Scenario: initSite task without site template or configuration site file
    Given a new Bakery project
    And I add a buildScript file with 'site.yml' as the config path in the dsl
    And does not have 'site.yml' for site configuration
    And I add the gradle settings file with gradle portal dependencies repository
    And the gradle project does not have 'jbake.properties' file for site
    And the gradle project does not have 'index.html' file for maquette
    When I am executing the task 'initSite'
    Then the project should have a 'site.yml' file for site configuration
    Then the project should have a directory named 'site' who contains 'jbake.properties' file
    Then the project should have a directory named 'maquette' who contains 'index.html' file
    Then the project should have a file named '.gitignore' who contains 'site.yml', '.gradle', 'build' and '.kotlin'
    Then the project should have a file named '.gitattributes' who contains 'eol' and 'crlf'

  Scenario: initSite task without site template or configuration site file with gradle.properties configured using 'bakery.configPath' without bakery dsl
    Given a new Bakery project
    And with buildScript file without bakery dsl
    And does not have 'site.yml' for site configuration
    And I add the gradle settings file with gradle portal dependencies repository
    And the gradle project does not have 'jbake.properties' file for site
    And the gradle project does not have 'index.html' file for maquette
    And I add gradle.properties file with the entry bakery.configPath='site.yml'
    When I am executing the task 'initSite'
    Then the project should have a 'site.yml' file for site configuration
    Then the project should have a directory named 'site' who contains 'jbake.properties' file
    Then the project should have a directory named 'maquette' who contains 'index.html' file
    Then the project should have a file named '.gitignore' who contains 'site.yml', '.gradle', 'build' and '.kotlin'
    Then the project should have a file named '.gitattributes' who contains 'eol' and 'crlf'
