@cucumber @bakery
Feature: The publishProfile task pushes profile files to GitHub

  Scenario: publishProfile task is registered when pushProfile is present in site.yml
    Given an existing Bakery project with pushProfile configuration
    And the output of the task 'tasks' contains 'publishProfile' from the group 'profile' and 'Push profile files (e.g. README.md) to GitHub repository'

  Scenario: publishProfile task is not registered when pushProfile is absent from site.yml
    Given an existing empty Bakery project using DSL with 'site.yml' file
    And the output of the task 'tasks' does not contain 'publishProfile'