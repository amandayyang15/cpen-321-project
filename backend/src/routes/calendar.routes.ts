import { Router } from 'express';
import { calendarController } from '../features/calendar/calendar.controller';
import { authenticateToken } from '../middleware/auth.middleware';

const router = Router();

// OAuth flow - callback doesn't need auth since Google redirects here
router.get('/oauth/callback', calendarController.handleOAuthCallback.bind(calendarController));
router.get('/oauth/authorize', authenticateToken, calendarController.authorizeCalendar.bind(calendarController));

// Calendar management - all require auth
router.get('/status', authenticateToken, calendarController.getCalendarStatus.bind(calendarController));
router.post('/enable', authenticateToken, calendarController.enableCalendar.bind(calendarController));
router.post('/disable', authenticateToken, calendarController.disableCalendar.bind(calendarController));
router.post('/disconnect', authenticateToken, calendarController.disconnectCalendar.bind(calendarController));

export default router;
