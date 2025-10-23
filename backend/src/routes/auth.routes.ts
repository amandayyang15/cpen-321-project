import { Router } from 'express';

import { AuthController } from '../features/auth/auth.controller';
import { AuthenticateUserRequest, authenticateUserSchema } from '../features/auth/auth.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const authController = new AuthController();

router.post(
  '/signup',
  validateBody<AuthenticateUserRequest>(authenticateUserSchema),
  authController.signUp
);

router.post(
  '/signin',
  validateBody(authenticateUserSchema),
  authController.signIn
);

export default router;
