@cucumber @bakery
Feature: The publishProfile task pushes profile files to GitHub

  Scenario: publishProfile task is registered when pushProfile is present in site.yml
    Given an existing Bakery project with pushProfile configuration
    And the output of the task 'tasks' contains 'publishProfile' from the group 'profile' and 'Push profile files (e.g. README.md) to GitHub repository'

  Scenario: publishProfile task is not registered when pushProfile is absent from site.yml
    Given an existing empty Bakery project using DSL with 'site.yml' file
    And the output of the task 'tasks' does not contain 'publishProfile'

  @end-to-end
  Scenario: publishProfile pushes profile files to a simulated remote and preserves history
    Given an existing Bakery project with pushProfile pointing to a simulated remote
    And the simulated remote has a file "old.txt" with content "existing remote content"
    And the project has profile files:
      | README.md    |
      | README_fr.md |
    When I execute the publishProfile task with credentials "testuser" and "testtoken"
    Then the simulated remote should contain "README.md"
    And the simulated remote should contain "README_fr.md"
    And the simulated remote should still contain "old.txt" with content "existing remote content"