#noinspection CucumberUndefinedStep
@cucumber @bakery
Feature: The site template contact form

  Scenario: `initSite` injects Firebase config into jbake.properties
    Given an existing empty Bakery project using DSL with 'site.yml' file
    And the output of the task 'tasks' contains 'initSite' from the group 'Bakery' and 'Initialise site and maquette folders.'
    When I am executing the task 'initSite'
    Then the project should have a directory named 'site' who contains 'jbake.properties' file
    And the 'jbake.properties' file in 'site' directory should contain 'firebaseApiKey' and 'firebaseProjectId'

