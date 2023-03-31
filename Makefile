local-env-init:
	docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql
	docker run -itd --name redis -p 6379:6379 redis


local-env-restart:
	docker restart mysql redis

local-env-stop:
	docker stop mysql redis