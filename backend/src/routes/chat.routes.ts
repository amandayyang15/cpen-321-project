import { Router } from 'express';
import { ChatController } from '../features/chat/chat.controller';
import { validateBody } from '../middleware/validation.middleware';
import { z } from 'zod';

const router = Router();
const chatController = new ChatController();

// Validation schemas
const sendMessageSchema = z.object({
  content: z.string().min(1, 'Message content is required').max(2000, 'Message content must be less than 2000 characters')
});

// Routes
router.post('/:projectId/messages', validateBody(sendMessageSchema), chatController.sendMessage);
router.get('/:projectId/messages', chatController.getMessages);
router.delete('/:projectId/messages/:messageId', chatController.deleteMessage);

export default router;
