Feature: Trainer Workload Messaging Integration
  As a system
  I want to ensure that workload messages are properly processed
  So that trainer workload data is accurately maintained

  Scenario: Process ADD workload message for existing trainer
    Given a trainer exists in the database with username "john.doe", first name "John", last name "Doe" and active status "true"
    When a "ADD" workload message is sent with username "john.doe", date "2023-10-15" and duration 120
    Then the workload message should be processed successfully
    And the trainer's data should be updated in the database

  Scenario: Process GET workload message to retrieve monthly data
    Given a trainer exists in the database with username "jane.smith", first name "Jane", last name "Smith" and active status "true"
    When a GET workload message is sent for username "jane.smith", year 2023 and month 10
    Then a response message should be received with username "jane.smith" and no errors
    And the response should contain year 2023, month 10 and a duration value

  Scenario: Process UPDATE workload message for existing trainer
    Given a trainer exists in the database with username "bob.johnson", first name "Bob", last name "Johnson" and active status "true"
    When a "UPDATE" workload message is sent with username "bob.johnson", date "2023-11-20" and duration 90
    Then the workload message should be processed successfully
    And the trainer's data should be updated in the database

  Scenario: Process DELETE workload message for existing trainer
    Given a trainer exists in the database with username "alice.brown", first name "Alice", last name "Brown" and active status "true"
    When a "DELETE" workload message is sent with username "alice.brown", date "2023-12-05" and duration 60
    Then the workload message should be processed successfully
    And the trainer's data should be updated in the database
