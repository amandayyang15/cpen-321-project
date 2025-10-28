ARG PORT
#
FROM node:20.6-alpine

WORKDIR /app/backend

COPY backend/package*.json ./

# RUN npm install
RUN npm ci

# COPY . .

# build typescript
COPY backend/ .
RUN npm run build

EXPOSE $PORT

CMD ["npm", "start"]
