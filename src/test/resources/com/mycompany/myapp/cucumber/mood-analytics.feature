Feature: Mood Analytics and Reporting
  
  As a user
  I want to analyze my mood patterns over time
  So that I can gain insights into my emotional wellbeing

  Background:
    Given the system is ready for mood analytics testing
    And test users are created

  Scenario: User views mood statistics for the current month
    Given I am logged in as "user1"
    And I have mood entries for the current month:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | HAPPY   |
      | 2024-01-04 | NEUTRAL |
      | 2024-01-05 | HAPPY   |
    When I request mood statistics for "2024-01"
    Then I should get mood statistics with:
      | metric           | value |
      | total_entries    | 5     |
      | happy_count      | 3     |
      | sad_count        | 1     |
      | neutral_count    | 1     |
      | most_common_mood | HAPPY |

  Scenario: User views mood trend over the last 7 days
    Given I am logged in as "user1"
    And I have mood entries for the last 7 days:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | HAPPY   |
      | 2024-01-04 | NEUTRAL |
      | 2024-01-05 | HAPPY   |
      | 2024-01-06 | ANXIOUS |
      | 2024-01-07 | HAPPY   |
    When I request mood trend for the last "7" days
    Then I should get a mood trend with "7" data points
    And the trend should show mood progression over time

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
    When I request streak information
    Then I should get current happy streak of "2" days
    And I should get longest happy streak of "3" days

  Scenario: User exports mood data for analysis
    Given I am logged in as "user1"
    And I have mood entries for the last 30 days
    When I request mood data export for "2024-01"
    Then I should receive a CSV file with mood data
    And the file should contain all my mood entries for the month

  Scenario: User compares mood patterns between time periods
    Given I am logged in as "user1"
    And I have mood entries for different periods:
      | date       | mood    |
      | 2024-01-01 | HAPPY   |
      | 2024-01-02 | SAD     |
      | 2024-01-03 | HAPPY   |
      | 2024-02-01 | NEUTRAL |
      | 2024-02-02 | HAPPY   |
      | 2024-02-03 | HAPPY   |
    When I compare mood patterns between "2024-01" and "2024-02"
    Then I should see improved mood trend in "2024-02"
    And the comparison should show percentage changes

  Scenario: User receives mood insights and recommendations
    Given I am logged in as "user1"
    And I have a pattern of anxious moods on Mondays
    When I request mood insights
    Then I should receive personalized mood insights
    And I should get recommendations for improving mood

  Scenario: User shares mood summary with healthcare provider
    Given I am logged in as "user1"
    And I have comprehensive mood data for the last 3 months
    When I generate a mood summary report for "2024-01" to "2024-03"
    Then I should get a professional mood summary report
    And the report should include mood trends and patterns 