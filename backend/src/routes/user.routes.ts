import { Router } from 'express';

import { UserController } from '../features/users/user.controller';
import { UpdateProfileRequest, updateProfileSchema } from '../features/users/user.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const userController = new UserController();

router.get('/profile', userController.getProfile);

router.post(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  userController.updateProfile
);

router.delete('/profile', userController.deleteProfile);

router.get('/:userId', userController.getUserById);

export default router;
