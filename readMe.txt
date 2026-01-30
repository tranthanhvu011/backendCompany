docker run -d --name redis -p 6379:6379 -v redis_data:/data redis redis-server --appendonly yes
docker exec -it redis redis-cli ping
