FROM node:18-alpine
WORKDIR /app

# Копируем зависимости и устанавливаем их
COPY package*.json ./
RUN npm install

# Копируем весь исходный код
COPY . .

# Запускаем сервер разработки React
CMD ["npm", "start"]