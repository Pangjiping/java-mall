local-env-init:
	docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -d mysql
	docker run -itd --name redis -p 6379:6379 redis
	docker run -d --name rabbitmq -p 5671:5671 -p 5672:5672 -p 4369:4369 -p 25672:25672 -p 15671:15671 -p 15672:15672 rabbitmq:management


local-env-restart:
	docker restart mysql redis rabbitmq

local-env-stop:
	docker stop mysql redis rabbitmq