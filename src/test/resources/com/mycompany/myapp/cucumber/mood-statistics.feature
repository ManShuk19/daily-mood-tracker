Feature: Mood Statistics and Analytics
  
  As a user
  I want to view statistics and analytics about my mood patterns
  So that I can understand my emotional wellbeing trends over time

  Background:
    Given the system is ready for mood statistics testing
    And test users are created

  Scenario: User views their mood statistics for the current month
    Given I am logged in as "user1"
    And I have mood entries for the current month:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | HAPPY   |
      | 2024-01-03 | SAD     |
      | 2024-01-04 | NEUTRAL |
      | 2024-01-05 | HAPPY   |
    When I request mood statistics for the current month
    Then I should get mood statistics with total entries of 5
    And the most frequent mood should be "HAPPY" with count 3
    And the mood distribution should show:
      | mood    | count |
      | HAPPY   | 3     |
      | SAD     | 1     |
      | NEUTRAL | 1     |

  Scenario: User views mood statistics for a specific date range
    Given I am logged in as "user1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | ANGRY   |
      | 2024-01-04 | HAPPY   |
      | 2024-01-05 | NEUTRAL |
      | 2024-01-06 | ANXIOUS |
      | 2024-01-07 | HAPPY   |
    When I request mood statistics from "2024-01-02" to "2024-01-06"
    Then I should get mood statistics with total entries of 5
    And the mood distribution should show:
      | mood    | count |
      | HAPPY   | 2     |
      | SAD     | 1     |
      | ANGRY   | 1     |
      | NEUTRAL | 1     |

  Scenario: User views mood trend analysis
    Given I am logged in as "user1"
    And I have mood entries over time:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | HAPPY   |
      | 2024-01-03 | SAD     |
      | 2024-01-04 | SAD     |
      | 2024-01-05 | NEUTRAL |
      | 2024-01-06 | HAPPY   |
      | 2024-01-07 | HAPPY   |
    When I request mood trend analysis for the last 7 days
    Then I should get trend analysis with positive trend
    And the trend should show improvement from "SAD" to "HAPPY"

  Scenario: User views mood streak information
    Given I am logged in as "user1"
    And I have consecutive mood entries:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | HAPPY   |
      | 2024-01-03 | HAPPY   |
      | 2024-01-04 | SAD     |
      | 2024-01-05 | HAPPY   |
      | 2024-01-06 | HAPPY   |
    When I request mood streak information
    Then I should get current streak of 2 "HAPPY" days
    And the longest streak should be 3 "HAPPY" days

  Scenario: User views weekly mood summary
    Given I am logged in as "user1"
    And I have mood entries for the current week:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | NEUTRAL |
      | 2024-01-04 | HAPPY   |
      | 2024-01-05 | ANXIOUS |
      | 2024-01-06 | HAPPY   |
      | 2024-01-07 | NEUTRAL |
    When I request weekly mood summary
    Then I should get weekly summary with average mood score
    And the most common mood for the week should be "HAPPY"
    And the mood frequency should be:
      | mood    | frequency |
      | HAPPY   | 3         |
      | NEUTRAL | 2         |
      | SAD     | 1         |
      | ANXIOUS | 1         |

  Scenario: User views monthly mood comparison
    Given I am logged in as "user1"
    And I have mood entries for December 2023:
      | date       | mood    |
      | 2023-12-01 | HAPPY   |
      | 2023-12-02 | SAD     |
      | 2023-12-03 | HAPPY   |
    And I have mood entries for January 2024:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | HAPPY   |
      | 2024-01-03 | HAPPY   |
      | 2024-01-04 | NEUTRAL |
    When I request monthly mood comparison between "2023-12" and "2024-01"
    Then I should get comparison showing improvement in January
    And January should have higher "HAPPY" mood percentage than December

  Scenario: User views mood statistics with no entries
    Given I am logged in as "user1"
    And I have no mood entries
    When I request mood statistics for the current month
    Then I should get empty mood statistics
    And the total entries should be 0
    And no mood distribution should be available

  Scenario: User views mood statistics for specific mood type
    Given I am logged in as "user1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | HAPPY   |
      | 2024-01-04 | SAD     |
      | 2024-01-05 | HAPPY   |
    When I request statistics for "HAPPY" mood only
    Then I should get statistics for "HAPPY" mood with count 3
    And the "HAPPY" mood percentage should be 60

  Scenario: User views mood statistics with missing days
    Given I am logged in as "user1"
    And I have mood entries with gaps:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-03 | SAD     |
      | 2024-01-05 | HAPPY   |
      | 2024-01-07 | NEUTRAL |
    When I request mood statistics for the week
    Then I should get statistics with 4 total entries
    And the tracking completion rate should be 57 percent
    And missing days should be identified

  Scenario: Multiple users have separate mood statistics
    Given I am logged in as "user1"
    And I have mood entries:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
    And user "user2" has mood entries:
      | date       | mood    |
      | 2024-01-01 | ANGRY   |
      | 2024-01-02 | HAPPY   |
      | 2024-01-03 | NEUTRAL |
    When I request my mood statistics
    Then I should get statistics with 2 total entries
    And the statistics should only include my mood entries
    And user "user2" should have 3 mood entries in their statistics 