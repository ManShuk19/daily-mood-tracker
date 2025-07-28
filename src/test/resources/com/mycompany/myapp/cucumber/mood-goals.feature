Feature: Mood Goals and Achievements
  
  As a user
  I want to set and track mood-related goals
  So that I can work towards improving my emotional wellbeing

  Background:
    Given the system is ready for mood goals testing
    And test users are created

  Scenario: User sets a mood improvement goal
    Given I am logged in as "user1"
    When I set a mood goal to have "5" happy days this week
    Then the mood goal should be saved successfully
    And I should see my goal in the goals dashboard

  Scenario: User tracks progress towards mood goal
    Given I am logged in as "user1"
    And I have a goal to have "5" happy days this week
    And I have logged "3" happy days so far this week
    When I check my goal progress
    Then I should see "3" out of "5" happy days completed
    And I should see "60%" progress towards my goal

  Scenario: User achieves a mood goal
    Given I am logged in as "user1"
    And I have a goal to have "5" happy days this week
    And I have logged "4" happy days so far this week
    When I log my "5th" happy day for the week
    Then I should receive a goal achievement notification
    And my goal should be marked as completed
    And I should earn achievement points

  Scenario: User sets a mood streak goal
    Given I am logged in as "user1"
    When I set a goal to maintain a "7" day happy streak
    Then the streak goal should be saved successfully
    And I should see my current streak progress

  Scenario: User breaks a mood streak goal
    Given I am logged in as "user1"
    And I have a "5" day happy streak goal
    And I currently have a "5" day happy streak
    When I log a "SAD" mood today
    Then my streak should be reset to "0"
    And I should receive a streak break notification
    And I should get encouragement to start a new streak

  Scenario: User sets multiple mood goals
    Given I am logged in as "user1"
    When I set multiple mood goals:
      | goal_type        | target | timeframe |
      | happy_days       | 5      | week      |
      | streak_days      | 7      | ongoing   |
      | mood_consistency | 80%    | month     |
    Then all goals should be saved successfully
    And I should see all goals in my dashboard

  Scenario: User receives goal reminders
    Given I am logged in as "user1"
    And I have a goal to have "5" happy days this week
    And it is "Friday" and I only have "3" happy days
    When the goal reminder system triggers
    Then I should receive a goal reminder notification
    And the reminder should encourage me to log happy moods

  Scenario: User views goal history and achievements
    Given I am logged in as "user1"
    And I have completed "3" mood goals in the past
    When I view my goal history
    Then I should see all my completed goals
    And I should see my achievement statistics
    And I should see my total achievement points

  Scenario: User sets a custom mood goal
    Given I am logged in as "user1"
    When I create a custom goal "Reduce anxious days by 50% this month"
    Then the custom goal should be saved successfully
    And I should be able to track progress towards this goal

  Scenario: User shares goal achievement
    Given I am logged in as "user1"
    And I have achieved a mood goal
    When I choose to share my achievement
    Then I should be able to share my achievement on social media
    And the shared content should include my achievement details

  Scenario: User receives goal suggestions based on mood patterns
    Given I am logged in as "user1"
    And I have a pattern of anxious moods on Mondays
    When I request goal suggestions
    Then I should receive personalized goal suggestions
    And the suggestions should include goals to reduce Monday anxiety

  Scenario: User adjusts goal difficulty
    Given I am logged in as "user1"
    And I have a goal to have "7" happy days this week
    And I find this goal too challenging
    When I adjust the goal to "5" happy days this week
    Then the goal should be updated successfully
    And the new target should be more achievable 