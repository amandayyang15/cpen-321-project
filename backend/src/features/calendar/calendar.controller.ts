import { Request, Response } from 'express';
import mongoose from 'mongoose';
import { calendarService } from './calendar.service';
import { userModel } from '../users/user.model';
import logger from '../../utils/logger.util';

export class CalendarController {
  /**
   * GET /api/calendar/oauth/authorize
   * Redirect user to Google OAuth consent screen
   */
  async authorizeCalendar(req: Request, res: Response): Promise<void> {
    try {
      const authUrl = calendarService.generateAuthUrl();
      
      // Store user ID in session or pass as state parameter
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }

      // Add state parameter with user ID to verify after redirect
      const urlWithState = `${authUrl}&state=${userId}`;
      
      res.json({ authUrl: urlWithState });
    } catch (error) {
      logger.error('Error generating auth URL:', error);
      res.status(500).json({ error: 'Failed to generate authorization URL' });
    }
  }

  /**
   * GET /api/calendar/oauth/callback
   * Handle OAuth callback from Google
   */
  async handleOAuthCallback(req: Request, res: Response): Promise<void> {
    try {
      const { code, state } = req.query;

      if (!code || typeof code !== 'string') {
        res.status(400).json({ error: 'Missing authorization code' });
        return;
      }

      if (!state || typeof state !== 'string') {
        res.status(400).json({ error: 'Missing state parameter' });
        return;
      }

      // Exchange code for tokens
      const tokens = await calendarService.getTokensFromCode(code);

      // Store refresh token in user document
      const userId = new mongoose.Types.ObjectId(state);
      await userModel.update(userId, {
        calendarRefreshToken: tokens.refreshToken,
        calendarEnabled: true,
      });

      logger.info(`Calendar connected for user: ${userId}`);

      // Redirect to a simple success page (HTML response)
      res.send(`
        <!DOCTYPE html>
        <html>
        <head>
          <title>Calendar Connected</title>
          <style>
            body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5; }
            .success { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 500px; margin: 0 auto; }
            h1 { color: #4CAF50; }
            p { color: #666; line-height: 1.6; }
            .icon { font-size: 60px; margin-bottom: 20px; }
          </style>
        </head>
        <body>
          <div class="success">
            <div class="icon">✅</div>
            <h1>Calendar Connected!</h1>
            <p>Your Google Calendar has been successfully connected.</p>
            <p>Tasks with deadlines will now automatically appear in your Google Calendar.</p>
            <p style="margin-top: 30px; color: #999;">You can close this window and return to your app.</p>
          </div>
        </body>
        </html>
      `);
    } catch (error) {
      logger.error('Error handling OAuth callback:', error);
      res.send(`
        <!DOCTYPE html>
        <html>
        <head>
          <title>Calendar Connection Failed</title>
          <style>
            body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: #f5f5f5; }
            .error { background: white; padding: 40px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 500px; margin: 0 auto; }
            h1 { color: #f44336; }
            p { color: #666; line-height: 1.6; }
            .icon { font-size: 60px; margin-bottom: 20px; }
          </style>
        </head>
        <body>
          <div class="error">
            <div class="icon">❌</div>
            <h1>Connection Failed</h1>
            <p>There was an error connecting your Google Calendar.</p>
            <p>Please try again or contact support if the problem persists.</p>
            <p style="margin-top: 30px; color: #999;">You can close this window and return to your app.</p>
          </div>
        </body>
        </html>
      `);
    }
  }

  /**
   * GET /api/calendar/status
   * Check if user has calendar enabled
   */
  async getCalendarStatus(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }

      const user = await userModel.findById(userId);
      
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      const isConnected = user.calendarEnabled && !!user.calendarRefreshToken;

      // Verify access if token exists
      let isValid = false;
      if (isConnected && user.calendarRefreshToken) {
        isValid = await calendarService.verifyAccess(user.calendarRefreshToken);
        
        // If token is invalid, disable calendar
        if (!isValid) {
          await userModel.update(userId, {
            calendarEnabled: false,
          });
        }
      }

      res.json({
        connected: isConnected && isValid,
        enabled: user.calendarEnabled,
      });
    } catch (error) {
      logger.error('Error getting calendar status:', error);
      res.status(500).json({ error: 'Failed to get calendar status' });
    }
  }

  /**
   * POST /api/calendar/enable
   * Enable calendar sync for user
   */
  async enableCalendar(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }

      const user = await userModel.findById(userId);
      
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      if (!user.calendarRefreshToken) {
        res.status(400).json({ 
          error: 'Calendar not connected. Please authorize first.' 
        });
        return;
      }

      await userModel.update(userId, {
        calendarEnabled: true,
      });

      logger.info(`Calendar enabled for user: ${userId}`);
      res.json({ message: 'Calendar sync enabled', enabled: true });
    } catch (error) {
      logger.error('Error enabling calendar:', error);
      res.status(500).json({ error: 'Failed to enable calendar sync' });
    }
  }

  /**
   * POST /api/calendar/disable
   * Disable calendar sync for user
   */
  async disableCalendar(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }

      await userModel.update(userId, {
        calendarEnabled: false,
      });

      logger.info(`Calendar disabled for user: ${userId}`);
      res.json({ message: 'Calendar sync disabled', enabled: false });
    } catch (error) {
      logger.error('Error disabling calendar:', error);
      res.status(500).json({ error: 'Failed to disable calendar sync' });
    }
  }

  /**
   * POST /api/calendar/disconnect
   * Disconnect calendar and revoke access
   */
  async disconnectCalendar(req: Request, res: Response): Promise<void> {
    try {
      const userId = req.user?._id;
      if (!userId) {
        res.status(401).json({ error: 'Unauthorized' });
        return;
      }

      const user = await userModel.findById(userId);
      
      if (!user) {
        res.status(404).json({ error: 'User not found' });
        return;
      }

      // Revoke access if refresh token exists
      if (user.calendarRefreshToken) {
        try {
          await calendarService.revokeAccess(user.calendarRefreshToken);
        } catch (error) {
          logger.error('Error revoking access:', error);
          // Continue even if revoke fails
        }
      }

      // Clear calendar data from user
      await userModel.update(userId, {
        calendarRefreshToken: undefined,
        calendarEnabled: false,
      });

      logger.info(`Calendar disconnected for user: ${userId}`);
      res.json({ message: 'Calendar disconnected successfully' });
    } catch (error) {
      logger.error('Error disconnecting calendar:', error);
      res.status(500).json({ error: 'Failed to disconnect calendar' });
    }
  }
}

export const calendarController = new CalendarController();
