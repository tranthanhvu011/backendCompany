docker run -d --name redis -p 6379:6379 -v redis_data:/data redis redis-server --appendonly yes
docker exec -it redis redis-cli ping
eval "local keys = redis.call('KEYS','*') for i,k in ipairs(keys) do keys[i] = k .. ' = ' .. redis.call('GET',k) end return keys" 0