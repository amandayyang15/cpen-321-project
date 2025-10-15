import { Router } from 'express';

import { upload } from '../utils/multer.config';
import { authenticateToken } from '../middleware/auth.middleware';
import { MediaController } from '../features/media/media.controller';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  authenticateToken,
  upload.single('media'),
  mediaController.uploadImage
);

export default router;
