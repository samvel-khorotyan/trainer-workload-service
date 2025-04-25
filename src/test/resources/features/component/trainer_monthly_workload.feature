Feature: Trainer Monthly Workload Retrieval
  As a system user
  I want to retrieve monthly workload data for trainers
  So that I can analyze their work patterns

  Background:
    Given a trainer with username "john.doe", first name "John", last name "Doe" and active status "true"

  Scenario: Retrieve existing monthly workload
    Given the trainer has a workload record for year 2023 month 9 with duration 150
    When I request monthly workload for username "john.doe" for year 2023 and month 9
    Then the monthly workload should show a total duration of 150
    And the monthly workload should have username "john.doe", first name "John", last name "Doe" and active status "true"

  Scenario: Retrieve monthly workload for a month with no data
    Given the trainer has a workload record for year 2023 month 10 with duration 180
    When I request monthly workload for username "john.doe" for year 2023 and month 11
    Then the monthly workload should show a total duration of 0

  Scenario: Retrieve monthly workload for non-existent trainer
    Given the trainer does not exist in the system
    When I request monthly workload for username "unknown.user" for year 2023 and month 12
    Then an empty workload with zero duration should be returned