Feature: Mood Tracking System
  
  As a user
  I want to track and analyze my daily moods
  So that I can understand my emotional patterns and improve my wellbeing

  Background:
    Given the mood tracking system is ready
    And test users are available

  Scenario: User creates their first mood entry
    Given I am logged in as "mooduser1"
    When I create a mood entry for today with mood "HAPPY"
    Then the mood entry should be created successfully
    And the mood entry should have mood "HAPPY"
    And the mood entry should have today's date
    And the mood entry should belong to "mooduser1"

  Scenario: User creates mood entry for specific date
    Given I am logged in as "mooduser1"
    When I create a mood entry for "2024-01-15" with mood "SAD"
    Then the mood entry should be created successfully
    And the mood entry should have mood "SAD"
    And the mood entry should have date "2024-01-15"

  Scenario: User cannot create duplicate mood entry for same date
    Given I am logged in as "mooduser1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I try to create another mood entry for "2024-01-15" with mood "SAD"
    Then the mood entry creation should fail
    And I should get a constraint violation error

  Scenario: User updates existing mood entry
    Given I am logged in as "mooduser1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I update the mood entry for "2024-01-15" to "ANXIOUS"
    Then the mood entry should be updated successfully
    And the mood entry should have mood "ANXIOUS"

  Scenario: User retrieves all their mood entries
    Given I am logged in as "mooduser1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
      | 2024-01-17 | NEUTRAL |
    When I retrieve all my mood entries
    Then I should get 3 mood entries
    And all mood entries should belong to me

  Scenario: User retrieves mood entry by date
    Given I am logged in as "mooduser1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
    When I retrieve mood entry for "2024-01-15"
    Then I should find the mood entry with mood "HAPPY"

  Scenario: User deletes a mood entry
    Given I am logged in as "mooduser1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    When I delete the mood entry for "2024-01-15"
    Then the mood entry should be deleted successfully
    And I should have 0 mood entries

  Scenario: Users can only access their own mood entries
    Given I am logged in as "mooduser1"
    And I have a mood entry for "2024-01-15" with mood "HAPPY"
    And user "mooduser2" has a mood entry for "2024-01-15" with mood "SAD"
    When I retrieve all my mood entries
    Then I should get 1 mood entry
    And the mood entry should have mood "HAPPY"
    And the mood entry should belong to "mooduser1"

  Scenario: Cannot create mood entry without date
    Given I am logged in as "mooduser1"
    When I try to create a mood entry without date
    Then the mood entry creation should fail
    And I should get a validation error

  Scenario: Cannot create mood entry without mood
    Given I am logged in as "mooduser1"
    When I try to create a mood entry for today without mood
    Then the mood entry creation should fail
    And I should get a validation error

  Scenario: Cannot create mood entry with invalid mood
    Given I am logged in as "mooduser1"
    When I try to create a mood entry for today with mood "INVALID_MOOD"
    Then the mood entry creation should fail
    And I should get a validation error

  Scenario: User creates mood entries with all valid mood types
    Given I am logged in as "mooduser1"
    When I create mood entries with all valid moods:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
      | 2024-01-17 | ANGRY   |
      | 2024-01-18 | NEUTRAL |
      | 2024-01-19 | ANXIOUS |
    Then all mood entries should be created successfully
    And I should have 5 mood entries with different moods

  Scenario: User retrieves mood entries for date range
    Given I am logged in as "mooduser1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | SAD     |
      | 2024-01-17 | NEUTRAL |
      | 2024-01-20 | ANXIOUS |
    When I retrieve mood entries from "2024-01-15" to "2024-01-17"
    Then I should get 3 mood entries
    And the mood entries should be within the date range

  Scenario: User gets mood statistics
    Given I am logged in as "mooduser1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-15 | HAPPY   |
      | 2024-01-16 | HAPPY   |
      | 2024-01-17 | SAD     |
      | 2024-01-18 | NEUTRAL |
    When I request mood statistics
    Then I should get mood statistics
    And the total mood entries should be 4
    And the most frequent mood should be "HAPPY" 