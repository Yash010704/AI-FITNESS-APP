import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router'
import { getActivity, getActivityRecommendation } from '../services/api';
import { Box, Card, CardContent, CircularProgress, Divider, Typography } from '@mui/material';

const ActivityDetail = () => {
  const { id } = useParams();
  const [activity, setActivity] = useState(null);
  const [recommendation, setRecommendation] = useState(null);
  const [polling, setPolling] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      // Fetch activity — always available immediately
      try {
        const activityRes = await getActivity(id);
        setActivity(activityRes.data);
      } catch (error) {
        console.error('Failed to fetch activity:', error);
      }

      // Poll for recommendation every 3s until it appears (max 10 attempts = 30s)
      let attempts = 0;
      const poll = setInterval(async () => {
        attempts++;
        try {
          const recRes = await getActivityRecommendation(id);
          setRecommendation(recRes.data);
          setPolling(false);
          clearInterval(poll); // got it, stop polling
        } catch (error) {
          if (error?.response?.status !== 404) {
            console.error('Failed to fetch recommendation:', error);
            setPolling(false);
            clearInterval(poll); // stop on unexpected errors
          }
          // 404 = still processing, keep polling
        }
        if (attempts >= 10) {
          setPolling(false);
          clearInterval(poll); // give up after 30s
        }
      }, 3000);

      return () => clearInterval(poll); // cleanup on unmount
    };

    fetchData();
  }, [id]);

  if (!activity) {
    return <Typography>Loading...</Typography>
  }

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', p: 2 }}>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h5" gutterBottom>Activity Details</Typography>
          <Typography>Type: {activity.type}</Typography>
          <Typography>Duration: {activity.duration} minutes</Typography>
          <Typography>Calories Burned: {activity.caloriesBurned}</Typography>
          <Typography>Date: {new Date(activity.createdAt).toLocaleString()}</Typography>
        </CardContent>
      </Card>

      {recommendation ? (
        <Card>
          <CardContent>
            <Typography variant="h5" gutterBottom>AI Recommendation</Typography>

            <Typography variant="h6">Analysis</Typography>
            <Typography paragraph>{recommendation.recommendation}</Typography>

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Improvements</Typography>
            {recommendation?.improvements?.map((improvement, index) => (
              <Typography key={index} paragraph>• {improvement}</Typography>
            ))}

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Suggestions</Typography>
            {recommendation?.suggestions?.map((suggestion, index) => (
              <Typography key={index} paragraph>• {suggestion}</Typography>
            ))}

            <Divider sx={{ my: 2 }} />

            <Typography variant="h6">Safety Guidelines</Typography>
            {recommendation?.safety?.map((s, index) => (
              <Typography key={index} paragraph>• {s}</Typography>
            ))}
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {polling && <CircularProgress size={20} />}
            <Typography variant="h6" color="text.secondary">
              {polling
                ? 'AI recommendation is being generated...'
                : 'AI recommendation could not be loaded. Please try again later.'}
            </Typography>
          </CardContent>
        </Card>
      )}
    </Box>
  )
}

export default ActivityDetail