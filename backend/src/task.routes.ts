import { Router } from 'express';
import { taskController } from './task.controller';
import { authenticateToken } from './auth.middleware';

const router = Router({ mergeParams: true });

router.post(
  '/projects/:projectId/tasks',
  authenticateToken,
  (req, res) => taskController.createTask(req, res)
);

export default router;
