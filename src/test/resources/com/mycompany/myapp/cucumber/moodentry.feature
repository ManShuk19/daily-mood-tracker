Feature: Mood Entry Management
  
  As a user
  I want to track my daily mood
  So that I can monitor my emotional wellbeing over time

  Background:
    Given the system is ready for mood entry testing
    And test users are created

  Scenario: User creates a mood entry for today
    Given I am logged in as "user1"
    When I create a mood entry for today with mood "HAPPY"
    Then the mood entry should be saved successfully
    And the mood entry should have mood "HAPPY"
    And the mood entry should have today's date

  Scenario: User creates mood entries for different dates
    Given I am logged in as "user1"
    When I create a mood entry for "2024-01-15" with mood "SAD"
    And I create a mood entry for "2024-01-16" with mood "HAPPY"
    Then both mood entries should be saved successfully
    And I should have 2 mood entries in total

  Scenario: User cannot create duplicate mood entries for same date
    Given I am logged in as "user1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I try to create another mood entry for "2024-01-15" with mood "SAD"
    Then the mood entry creation should fail
    And I should get a constraint violation error

  Scenario: User updates existing mood entry
    Given I am logged in as "user1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I update the mood entry to "ANXIOUS"
    Then the mood entry should be updated successfully
    And the mood entry should have mood "ANXIOUS"

  Scenario: User retrieves their mood entries
    Given I am logged in as "user1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
      | 2024-01-17 | NEUTRAL |
    When I retrieve my mood entries
    Then I should get 3 mood entries
    And the mood entries should belong to me

  Scenario: User deletes a mood entry
    Given I am logged in as "user1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I delete the mood entry for "2024-01-15"
    Then the mood entry should be deleted successfully
    And I should have 0 mood entries

  Scenario: Users can only see their own mood entries
    Given I am logged in as "user1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    And user "user2" has a mood entry for "2024-01-15" with mood "SAD"
    When I retrieve my mood entries
    Then I should get 1 mood entry
    And the mood entry should have mood "HAPPY"
    And the mood entry should belong to "user1"

  Scenario: Cannot create mood entry without required fields
    Given I am logged in as "user1"
    When I try to create a mood entry without date
    Then the mood entry creation should fail
    And I should get a validation error

  Scenario: Cannot create mood entry with null mood
    Given I am logged in as "user1"
    When I try to create a mood entry for today without mood
    Then the mood entry creation should fail
    And I should get a validation error

  Scenario: User retrieves mood entry by specific date
    Given I am logged in as "user1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
    When I search for mood entry on "2024-01-15"
    Then I should find the mood entry with mood "HAPPY"

  Scenario: Mood entry supports all valid mood types
    Given I am logged in as "user1"
    When I create mood entries with all valid moods:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
      | 2024-01-17 | ANGRY   |
      | 2024-01-18 | NEUTRAL |
      | 2024-01-19 | ANXIOUS |
    Then all mood entries should be saved successfully
    And I should have 5 mood entries with different moods 