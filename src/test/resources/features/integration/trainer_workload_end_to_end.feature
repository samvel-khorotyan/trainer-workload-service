Feature: Trainer Workload End-to-End Flow
  As a system
  I want to ensure the complete workflow functions correctly
  So that trainer workload data is accurately maintained and retrieved

  Scenario: Complete workflow from adding workload to retrieving monthly summary
    Given a trainer exists in the database with username "complete.flow", first name "Complete", last name "Flow" and active status "true"
    When a "ADD" workload message is sent with username "complete.flow", date "2023-10-10" and duration 120
    And a "ADD" workload message is sent with username "complete.flow", date "2023-10-15" and duration 90
    And a "ADD" workload message is sent with username "complete.flow", date "2023-10-20" and duration 60
    And a GET workload message is sent for username "complete.flow", year 2023 and month 10
    Then a response message should be received with username "complete.flow" and no errors
    And the response should contain year 2023, month 10 and a duration value of 270

  Scenario: Update and delete workload entries then verify monthly summary
    Given a trainer exists in the database with username "update.flow", first name "Update", last name "Flow" and active status "true"
    When a "ADD" workload message is sent with username "update.flow", date "2023-11-10" and duration 100
    And a "UPDATE" workload message is sent with username "update.flow", date "2023-11-10" and duration 80
    And a "ADD" workload message is sent with username "update.flow", date "2023-11-15" and duration 60
    And a "DELETE" workload message is sent with username "update.flow", date "2023-11-15" and duration 30
    And a GET workload message is sent for username "update.flow", year 2023 and month 11
    Then a response message should be received with username "update.flow" and no errors
    And the response should contain year 2023, month 11 and a duration value of 110
