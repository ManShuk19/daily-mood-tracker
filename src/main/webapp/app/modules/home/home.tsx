import './home.scss';

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Alert, Col, Row, Card, CardBody, CardTitle, Button, Badge, Progress } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSmile, faFrown, faAngry, faMeh, faFlushed, faPlus, faChartLine, faCalendarAlt } from '@fortawesome/free-solid-svg-icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { useAppSelector } from 'app/config/store';
import axios from 'axios';

const COLORS = ['#28a745', '#6c757d', '#dc3545', '#ffc107', '#17a2b8'];

export const Home = () => {
  const account = useAppSelector(state => state.authentication.account);
  const [todayMood, setTodayMood] = useState(null);
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (account?.login) {
      loadTodayMood();
      loadStatistics();
    }
  }, [account]);

  const loadTodayMood = async () => {
    try {
      const today = new Date().toISOString().split('T')[0];
      const response = await axios.get(`/api/mood-entries/my/date/${today}`);
      setTodayMood(response.data);
    } catch (error) {
      // No mood entry for today
      setTodayMood(null);
    }
  };

  const loadStatistics = async () => {
    try {
      const response = await axios.get('/api/mood-entries/statistics/week');
      setStatistics(response.data);
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  const handleMoodSelect = async mood => {
    setLoading(true);
    try {
      const today = new Date().toISOString().split('T')[0];
      const moodData = {
        date: today,
        mood,
        user: { id: account.id },
      };

      if (todayMood) {
        // Update existing entry
        await axios.put(`/api/mood-entries/${todayMood.id}`, moodData);
      } else {
        // Create new entry
        await axios.post('/api/mood-entries', moodData);
      }

      await loadTodayMood();
      await loadStatistics();
    } catch (error) {
      console.error('Error saving mood:', error);
    } finally {
      setLoading(false);
    }
  };

  const getMoodIcon = mood => {
    switch (mood) {
      case 'HAPPY':
        return faSmile;
      case 'SAD':
        return faFrown;
      case 'ANGRY':
        return faAngry;
      case 'NEUTRAL':
        return faMeh;
      case 'ANXIOUS':
        return faFlushed;
      default:
        return faMeh;
    }
  };

  const getMoodColor = mood => {
    switch (mood) {
      case 'HAPPY':
        return 'success';
      case 'SAD':
        return 'danger';
      case 'ANGRY':
        return 'warning';
      case 'NEUTRAL':
        return 'secondary';
      case 'ANXIOUS':
        return 'info';
      default:
        return 'secondary';
    }
  };

  const getMoodLabel = mood => {
    switch (mood) {
      case 'HAPPY':
        return 'Happy';
      case 'SAD':
        return 'Sad';
      case 'ANGRY':
        return 'Angry';
      case 'NEUTRAL':
        return 'Neutral';
      case 'ANXIOUS':
        return 'Anxious';
      default:
        return mood;
    }
  };

  const prepareChartData = () => {
    if (!statistics?.trends) return [];
    return statistics.trends.map(trend => ({
      date: new Date(trend.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
      score: trend.moodScore,
    }));
  };

  const preparePieData = () => {
    if (!statistics?.moodDistribution) return [];
    return Object.entries(statistics.moodDistribution).map(([mood, count]) => ({
      name: getMoodLabel(mood),
      value: count,
    }));
  };

  if (!account?.login) {
    return (
      <Row>
        <Col md="12">
          <div className="text-center">
            <h1 className="display-4 mb-4">Welcome to Daily Mood Tracker</h1>
            <p className="lead mb-4">Track your daily moods and gain insights into your emotional well-being</p>
            <Alert color="warning" className="mb-4">
              <Link to="/login" className="alert-link">
                Sign in
              </Link>{' '}
              to start tracking your moods, or{' '}
              <Link to="/account/register" className="alert-link">
                register a new account
              </Link>
            </Alert>
            <div className="row justify-content-center">
              <div className="col-md-4">
                <Card className="text-center">
                  <CardBody>
                    <FontAwesomeIcon icon={faSmile} size="3x" className="text-success mb-3" />
                    <h5>Track Daily Moods</h5>
                    <p className="text-muted">Log your mood every day to build a comprehensive emotional history</p>
                  </CardBody>
                </Card>
              </div>
              <div className="col-md-4">
                <Card className="text-center">
                  <CardBody>
                    <FontAwesomeIcon icon={faChartLine} size="3x" className="text-primary mb-3" />
                    <h5>View Analytics</h5>
                    <p className="text-muted">See trends, patterns, and insights about your emotional well-being</p>
                  </CardBody>
                </Card>
              </div>
              <div className="col-md-4">
                <Card className="text-center">
                  <CardBody>
                    <FontAwesomeIcon icon={faCalendarAlt} size="3x" className="text-info mb-3" />
                    <h5>Monitor Progress</h5>
                    <p className="text-muted">Track your mood streaks and overall emotional health over time</p>
                  </CardBody>
                </Card>
              </div>
            </div>
          </div>
        </Col>
      </Row>
    );
  }

  return (
    <div className="mood-dashboard">
      <Row>
        <Col md="12">
          <div className="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h1 className="display-5 mb-2">Welcome back, {account.firstName || account.login}!</h1>
              <p className="text-muted">How are you feeling today?</p>
            </div>
            <Link to="/mood-entry/new" className="btn btn-primary">
              <FontAwesomeIcon icon={faPlus} /> Add Mood Entry
            </Link>
          </div>
        </Col>
      </Row>

      {/* Quick Mood Selection */}
      <Row className="mb-4">
        <Col md="12">
          <Card>
            <CardBody>
              <CardTitle tag="h5" className="mb-3">
                {todayMood ? "Update Today's Mood" : "Log Today's Mood"}
              </CardTitle>
              <div className="d-flex justify-content-center gap-3 flex-wrap">
                {['HAPPY', 'NEUTRAL', 'ANXIOUS', 'SAD', 'ANGRY'].map(mood => (
                  <Button
                    key={mood}
                    color={todayMood?.mood === mood ? getMoodColor(mood) : 'outline-' + getMoodColor(mood)}
                    size="lg"
                    className="mood-button"
                    onClick={() => handleMoodSelect(mood)}
                    disabled={loading}
                  >
                    <FontAwesomeIcon icon={getMoodIcon(mood)} size="2x" className="mb-2" />
                    <br />
                    {getMoodLabel(mood)}
                  </Button>
                ))}
              </div>
              {todayMood && (
                <div className="text-center mt-3">
                  <Badge color={getMoodColor(todayMood.mood)} size="lg">
                    Today&apos;s mood: {getMoodLabel(todayMood.mood)}
                  </Badge>
                </div>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* Statistics Overview */}
      {statistics && (
        <Row className="mb-4">
          <Col md="3">
            <Card className="text-center">
              <CardBody>
                <h3 className="text-primary">{statistics.totalEntries}</h3>
                <p className="text-muted mb-0">Total Entries</p>
              </CardBody>
            </Card>
          </Col>
          <Col md="3">
            <Card className="text-center">
              <CardBody>
                <h3 className="text-success">{statistics.averageMoodScore?.toFixed(1) || '0'}</h3>
                <p className="text-muted mb-0">Average Score</p>
              </CardBody>
            </Card>
          </Col>
          <Col md="3">
            <Card className="text-center">
              <CardBody>
                <h3 className="text-warning">{statistics.currentStreak || '0'}</h3>
                <p className="text-muted mb-0">Current Streak</p>
              </CardBody>
            </Card>
          </Col>
          <Col md="3">
            <Card className="text-center">
              <CardBody>
                <h3 className="text-info">{statistics.trackingCompletionRate?.toFixed(0) || '0'}%</h3>
                <p className="text-muted mb-0">Completion Rate</p>
              </CardBody>
            </Card>
          </Col>
        </Row>
      )}

      {/* Charts */}
      <Row>
        <Col md="8">
          <Card>
            <CardBody>
              <CardTitle tag="h5" className="mb-3">
                Mood Trends (Last 7 Days)
              </CardTitle>
              <ResponsiveContainer width="100%" height={300}>
                <LineChart data={prepareChartData()}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis domain={[0, 5]} />
                  <Tooltip />
                  <Line type="monotone" dataKey="score" stroke="#8884d8" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </CardBody>
          </Card>
        </Col>
        <Col md="4">
          <Card>
            <CardBody>
              <CardTitle tag="h5" className="mb-3">
                Mood Distribution
              </CardTitle>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={preparePieData()}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={80}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {preparePieData().map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </CardBody>
          </Card>
        </Col>
      </Row>

      {/* Quick Actions */}
      <Row className="mt-4">
        <Col md="12">
          <Card>
            <CardBody>
              <CardTitle tag="h5" className="mb-3">
                Quick Actions
              </CardTitle>
              <div className="d-flex gap-3 flex-wrap">
                <Link to="/mood-entry" className="btn btn-outline-primary">
                  <FontAwesomeIcon icon={faCalendarAlt} /> View All Entries
                </Link>
                <Link to="/mood-entry/new" className="btn btn-outline-success">
                  <FontAwesomeIcon icon={faPlus} /> Add New Entry
                </Link>
                <Link to="/account/settings" className="btn btn-outline-secondary">
                  <FontAwesomeIcon icon={faChartLine} /> Account Settings
                </Link>
              </div>
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Home;
