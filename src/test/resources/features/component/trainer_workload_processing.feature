Feature: Trainer Workload Processing
  As a system administrator
  I want to process trainer workload data
  So that I can track and manage trainer activities

  Background:
    Given a trainer with username "john.doe", first name "John", last name "Doe" and active status "true"

  Scenario: Add new training hours to existing trainer
    Given the trainer has a workload record for year 2023 month 5 with duration 120
    When I process a "ADD" workload command for username "john.doe" with date "2023-05-15" and duration 60
    Then the trainer workload should be saved with updated duration 180 for year 2023 month 5

  Scenario: Update training hours for existing trainer
    Given the trainer has a workload record for year 2023 month 6 with duration 120
    When I process a "UPDATE" workload command for username "john.doe" with date "2023-06-15" and duration 90
    Then the trainer workload should be saved with updated duration 90 for year 2023 month 6

  Scenario: Delete training hours for existing trainer
    Given the trainer has a workload record for year 2023 month 7 with duration 120
    When I process a "DELETE" workload command for username "john.doe" with date "2023-07-15" and duration 50
    Then the trainer workload should be saved with updated duration 70 for year 2023 month 7

  Scenario: Create new trainer record when processing workload for non-existent trainer
    Given the trainer does not exist in the system
    When I process a "ADD" workload command for username "jane.smith" with date "2023-08-15" and duration 75
    Then a new trainer workload record should be created