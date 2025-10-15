import { Router } from 'express';

import { HobbyController } from '../features/hobbies/hobby.controller';

const router = Router();
const hobbyController = new HobbyController();

router.get('/', hobbyController.getAllHobbies);

export default router;
