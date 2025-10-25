import { Router } from 'express';
import mongoose from 'mongoose';
import { userModel } from '../features/users/user.model';
import logger from '../utils/logger.util';

const router = Router();

/**
 * DEV ONLY: Enable calendar without OAuth for testing
 * DELETE THIS FILE BEFORE PRODUCTION!
 */
router.post('/dev/enable-calendar-test', async (req, res) => {
  try {
    const userId = req.user?._id;
    if (!userId) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }

    // Set a dummy refresh token for testing
    // In production, this would come from OAuth
    const dummyRefreshToken = 'test_token_' + Date.now();
    
    await userModel.update(userId, {
      calendarRefreshToken: dummyRefreshToken,
      calendarEnabled: true,
    });

    logger.info(`⚠️ DEV MODE: Calendar enabled for user ${userId} with test token`);
    
    res.json({ 
      message: 'Calendar enabled for testing (no real Google Calendar sync)',
      warning: 'This is for development only. Real calendar sync requires OAuth.',
      enabled: true 
    });
  } catch (error) {
    logger.error('Error enabling test calendar:', error);
    res.status(500).json({ error: 'Failed to enable calendar' });
  }
});

/**
 * DEV ONLY: Disable calendar
 */
router.post('/dev/disable-calendar-test', async (req, res) => {
  try {
    const userId = req.user?._id;
    if (!userId) {
      res.status(401).json({ error: 'Unauthorized' });
      return;
    }

    await userModel.update(userId, {
      calendarRefreshToken: undefined,
      calendarEnabled: false,
    });

    logger.info(`⚠️ DEV MODE: Calendar disabled for user ${userId}`);
    
    res.json({ message: 'Calendar disabled', enabled: false });
  } catch (error) {
    logger.error('Error disabling calendar:', error);
    res.status(500).json({ error: 'Failed to disable calendar' });
  }
});

export default router;
