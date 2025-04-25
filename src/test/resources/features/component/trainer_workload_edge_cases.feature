Feature: Trainer Workload Edge Cases
  As a system administrator
  I want to ensure the system handles edge cases properly
  So that the system remains robust under unusual conditions

  Background:
    Given a trainer with username "john.doe", first name "John", last name "Doe" and active status "true"

  Scenario: Process workload with zero duration
    Given the trainer has a workload record for year 2023 month 5 with duration 0
    When I process a "ADD" workload command for username "john.doe" with date "2023-05-15" and duration 0
    Then the trainer workload should be saved with updated duration 0 for year 2023 month 5

  Scenario: Process workload for future date
    Given the trainer has a workload record for year 2030 month 1 with duration 0
    When I process a "ADD" workload command for username "john.doe" with date "2030-01-01" and duration 60
    Then the trainer workload should be saved with updated duration 60 for year 2030 month 1

  Scenario: Delete more hours than available
    Given the trainer has a workload record for year 2023 month 8 with duration 30
    When I process a "DELETE" workload command for username "john.doe" with date "2023-08-15" and duration 50
    Then the trainer workload should be saved with updated duration 0 for year 2023 month 8

  Scenario: Process multiple actions for the same month
    Given the trainer has a workload record for year 2023 month 9 with duration 100
    When I process a "ADD" workload command for username "john.doe" with date "2023-09-15" and duration 50
    And I process a "ADD" workload command for username "john.doe" with date "2023-09-20" and duration 30
    Then the final trainer workload should be 180 for year 2023 month 9