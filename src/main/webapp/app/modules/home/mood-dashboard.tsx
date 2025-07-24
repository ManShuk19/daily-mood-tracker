import React, { useEffect, useState } from 'react';
import { Card, CardBody, CardHeader, Col, Row, Button, Alert } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getEntities as getMoodEntries, createEntity } from 'app/entities/mood-entry/mood-entry.reducer';
import { MoodType } from 'app/shared/model/enumerations/mood-type.model';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import dayjs from 'dayjs';
import './mood-dashboard.scss';

interface MoodEntry {
  id: number;
  date: string;
  mood: string;
  user?: any;
}

interface ChartDataPoint {
  date: string;
  mood: string;
  moodValue: number;
}

interface MoodStat {
  mood: string;
  count: number;
  emoji: string;
  color: string;
}

const moodEmojis: Record<string, string> = {
  HAPPY: '😊',
  SAD: '😢',
  ANGRY: '😠',
  NEUTRAL: '😐',
  ANXIOUS: '😰',
};

const moodColors: Record<string, string> = {
  HAPPY: '#28a745',
  SAD: '#007bff',
  ANGRY: '#dc3545',
  NEUTRAL: '#6c757d',
  ANXIOUS: '#ffc107',
};

export const MoodDashboard = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const moodEntries = useAppSelector(state => state.moodEntry.entities) as MoodEntry[];
  const loading = useAppSelector(state => state.moodEntry.loading);
  const [selectedMood, setSelectedMood] = useState<MoodType | null>(null);
  const [todayEntry, setTodayEntry] = useState<MoodEntry | null>(null);

  useEffect(() => {
    if (account?.login) {
      dispatch(getMoodEntries({ size: 30, sort: 'date,desc' }));
    }
  }, [account, dispatch]);

  useEffect(() => {
    const today = dayjs().format('YYYY-MM-DD');
    const todayMoodEntry = moodEntries.find(entry => entry.date === today);
    setTodayEntry(todayMoodEntry || null);
  }, [moodEntries]);

  const handleMoodSubmit = () => {
    if (selectedMood) {
      const today = dayjs();
      dispatch(
        createEntity({
          date: today,
          mood: selectedMood,
          user: account,
        }),
      );
      setSelectedMood(null);
    }
  };

  const getChartData = (): ChartDataPoint[] => {
    return moodEntries
      .slice(0, 7)
      .reverse()
      .map(entry => ({
        date: dayjs(entry.date).format('MMM DD'),
        mood: entry.mood,
        moodValue: getMoodValue(entry.mood),
      }));
  };

  const getMoodValue = (mood: string): number => {
    switch (mood) {
      case 'HAPPY':
        return 5;
      case 'NEUTRAL':
        return 3;
      case 'SAD':
        return 2;
      case 'ANGRY':
        return 1;
      case 'ANXIOUS':
        return 2;
      default:
        return 3;
    }
  };

  const getMoodStats = (): MoodStat[] => {
    const last30Days = moodEntries.slice(0, 30);
    const moodCounts: Record<string, number> = {};

    last30Days.forEach(entry => {
      moodCounts[entry.mood] = (moodCounts[entry.mood] || 0) + 1;
    });

    return Object.entries(moodCounts).map(([mood, count]) => ({
      mood,
      count,
      emoji: moodEmojis[mood] || '😐',
      color: moodColors[mood] || '#6c757d',
    }));
  };

  const getMaxCount = (): number => {
    const stats = getMoodStats();
    return Math.max(...stats.map(s => s.count), 1);
  };

  if (!account?.login) {
    return (
      <div className="mood-dashboard">
        <Row className="justify-content-center">
          <Col md="8" className="text-center">
            <Alert color="info">Please log in to start tracking your daily mood.</Alert>
          </Col>
        </Row>
      </div>
    );
  }

  return (
    <div className="mood-dashboard">
      <Row className="mb-4">
        <Col md="12">
          <h1 className="dashboard-title">
            <FontAwesomeIcon icon="heart" className="me-2" />
            Daily Mood Tracker
          </h1>
          <p className="dashboard-subtitle">Welcome back, {account.login}! How are you feeling today?</p>
        </Col>
      </Row>

      <Row className="mb-4">
        <Col md="12">
          <Card className="mood-entry-card">
            <CardHeader>
              <h4>
                <FontAwesomeIcon icon="plus-circle" className="me-2" />
                Log Today&apos;s Mood
              </h4>
            </CardHeader>
            <CardBody>
              {todayEntry ? (
                <Alert color="success">
                  <FontAwesomeIcon icon="check-circle" className="me-2" />
                  You&apos;ve already logged your mood for today:{' '}
                  <strong>
                    {moodEmojis[todayEntry.mood]} {todayEntry.mood}
                  </strong>
                </Alert>
              ) : (
                <div>
                  <p className="mb-3">How are you feeling today?</p>
                  <div className="mood-selector">
                    {Object.entries(moodEmojis).map(([mood, emoji]) => (
                      <button
                        key={mood}
                        className={`mood-button ${selectedMood === (mood as MoodType) ? 'selected' : ''}`}
                        onClick={() => setSelectedMood(mood as MoodType)}
                        data-cy={`mood-${mood.toLowerCase()}`}
                      >
                        <span className="mood-emoji">{emoji}</span>
                        <span className="mood-label">{mood}</span>
                      </button>
                    ))}
                  </div>
                  {selectedMood && (
                    <div className="mt-3">
                      <Button color="primary" onClick={handleMoodSubmit} data-cy="submit-mood">
                        <FontAwesomeIcon icon="save" className="me-2" />
                        Log My Mood
                      </Button>
                    </div>
                  )}
                </div>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>

      <Row>
        <Col md="8">
          <Card className="mood-chart-card">
            <CardHeader>
              <h4>
                <FontAwesomeIcon icon="chart-line" className="me-2" />
                Mood Trend (Last 7 Days)
              </h4>
            </CardHeader>
            <CardBody>
              {moodEntries.length > 0 ? (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={getChartData()}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis
                      domain={[0, 6]}
                      tickFormatter={(value: number) => {
                        const labels = ['', 'Very Low', 'Low', 'Neutral', 'Good', 'Great'];
                        return labels[value] || '';
                      }}
                    />
                    <Tooltip
                      formatter={(value: number) => {
                        const labels = ['', 'Very Low', 'Low', 'Neutral', 'Good', 'Great'];
                        return [labels[value] || value, 'Mood Level'];
                      }}
                    />
                    <Line
                      type="monotone"
                      dataKey="moodValue"
                      stroke="#007bff"
                      strokeWidth={3}
                      dot={{ fill: '#007bff', strokeWidth: 2, r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="text-center py-5">
                  <FontAwesomeIcon icon="chart-line" size="3x" className="text-muted mb-3" />
                  <p className="text-muted">Start logging your moods to see trends here</p>
                </div>
              )}
            </CardBody>
          </Card>
        </Col>

        <Col md="4">
          <Card className="mood-stats-card">
            <CardHeader>
              <h4>
                <FontAwesomeIcon icon="chart-bar" className="me-2" />
                Mood Summary (30 Days)
              </h4>
            </CardHeader>
            <CardBody>
              {moodEntries.length > 0 ? (
                <div className="mood-stats">
                  {getMoodStats().map(stat => (
                    <div key={stat.mood} className="mood-stat-item">
                      <div className="mood-stat-emoji">{stat.emoji}</div>
                      <div className="mood-stat-info">
                        <div className="mood-stat-mood">{stat.mood}</div>
                        <div className="mood-stat-count">{stat.count} times</div>
                      </div>
                      <div
                        className="mood-stat-bar"
                        style={{
                          width: `${(stat.count / getMaxCount()) * 100}%`,
                          backgroundColor: stat.color,
                        }}
                      ></div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-3">
                  <FontAwesomeIcon icon="chart-bar" size="2x" className="text-muted mb-2" />
                  <p className="text-muted">No mood data yet</p>
                </div>
              )}
            </CardBody>
          </Card>

          <Card className="mt-3 recent-entries-card">
            <CardHeader>
              <h4>
                <FontAwesomeIcon icon="history" className="me-2" />
                Recent Entries
              </h4>
            </CardHeader>
            <CardBody>
              {moodEntries.slice(0, 5).map(entry => (
                <div key={entry.id} className="recent-entry">
                  <div className="recent-entry-date">{dayjs(entry.date).format('MMM DD')}</div>
                  <div className="recent-entry-mood">
                    <span className="mood-emoji-small">{moodEmojis[entry.mood]}</span>
                    <span className="mood-name">{entry.mood}</span>
                  </div>
                </div>
              ))}
              {moodEntries.length === 0 && (
                <div className="text-center py-2">
                  <FontAwesomeIcon icon="calendar" className="text-muted mb-2" />
                  <p className="text-muted small">No entries yet</p>
                </div>
              )}
            </CardBody>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default MoodDashboard;
