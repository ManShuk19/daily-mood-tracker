Feature: Mood Reminders and Notifications
  
  As a user
  I want to receive reminders to track my mood
  So that I can maintain consistent mood tracking habits

  Background:
    Given the system is ready for mood reminder testing
    And test users are created

  Scenario: User sets up daily mood reminder
    Given I am logged in as "user1"
    When I set up a daily mood reminder at "18:00"
    Then the mood reminder should be saved successfully
    And I should receive a confirmation message

  Scenario: User receives mood reminder notification
    Given I am logged in as "user1"
    And I have a daily mood reminder set for "18:00"
    And it is "18:00" and I haven't logged my mood today
    When the reminder system triggers
    Then I should receive a mood reminder notification
    And the notification should prompt me to log my mood

  Scenario: User dismisses mood reminder
    Given I am logged in as "user1"
    And I have a daily mood reminder set for "18:00"
    And I receive a mood reminder notification
    When I dismiss the reminder
    Then the reminder should be marked as dismissed
    And I should not receive another reminder today

  Scenario: User completes mood entry after reminder
    Given I am logged in as "user1"
    And I have a daily mood reminder set for "18:00"
    And I receive a mood reminder notification
    When I log my mood as "HAPPY" after receiving the reminder
    Then the reminder should be marked as completed
    And I should not receive another reminder today

  Scenario: User sets up weekly mood summary reminder
    Given I am logged in as "user1"
    When I set up a weekly mood summary reminder for "Sunday" at "10:00"
    Then the weekly reminder should be saved successfully
    And I should receive a confirmation message

  Scenario: User receives weekly mood summary
    Given I am logged in as "user1"
    And I have a weekly mood summary reminder set for "Sunday" at "10:00"
    And I have mood entries for the past week
    When it is "Sunday" at "10:00"
    Then I should receive a weekly mood summary notification
    And the summary should include my mood statistics for the week

  Scenario: User customizes reminder preferences
    Given I am logged in as "user1"
    When I update my reminder preferences:
      | setting           | value    |
      | daily_reminder    | 19:30    |
      | weekly_summary    | Saturday |
      | notification_type | email    |
    Then my reminder preferences should be updated successfully
    And the new settings should be applied

  Scenario: User disables mood reminders
    Given I am logged in as "user1"
    And I have mood reminders enabled
    When I disable mood reminders
    Then mood reminders should be disabled
    And I should not receive any mood reminder notifications

  Scenario: User sets up mood streak reminders
    Given I am logged in as "user1"
    And I have a 5-day happy mood streak
    When I set up a streak reminder for "7" days
    Then the streak reminder should be saved successfully
    And I should be notified when I reach "7" consecutive happy days

  Scenario: User receives motivational reminder after negative mood
    Given I am logged in as "user1"
    And I logged a "SAD" mood yesterday
    When it is time for my daily reminder
    Then I should receive a motivational reminder
    And the reminder should include encouraging content

  Scenario: User sets up custom reminder messages
    Given I am logged in as "user1"
    When I set a custom reminder message "Time to check in with yourself!"
    Then the custom message should be saved successfully
    And future reminders should use my custom message 