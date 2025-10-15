import { Router } from 'express';

import { authenticateToken } from './auth.middleware';
import authRoutes from './auth.routes';
import hobbiesRoutes from './hobbies.routes';
import mediaRoutes from './media.routes';
import projectRoutes from './project.routes';
import usersRoutes from './user.routes';
import taskRoutes from './task.routes';

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/projects', authenticateToken, projectRoutes);

router.use('/tasks', authenticateToken, taskRoutes);


export default router;
