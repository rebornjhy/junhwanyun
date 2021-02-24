# Command

---

## Docker

### aws config
AKIAXHDEFLPIQYZ3S2O2
1kHeRzUU/CUHTImiRar22kdrpJyMlH/X7I/J+aPo
ap-northeast-1

### docker login
docker login --username AWS -p $(aws ecr get-login-password --region ap-northeast-1) 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/

### create eks cluster
eksctl create cluster --name junhwanyun --version 1.17 --nodegroup-name standard-workers --node-type t3.medium --nodes 3 --nodes-min 1 --nodes-max 3

### eks config
aws eks --region ap-northeast-1 update-kubeconfig --name junhwanyun

### create ecr repository
aws ecr create-repository --repository-name junhwanyun-admin --image-scanning-configuration scanOnPush=true --region ap-northeast-1
aws ecr create-repository --repository-name junhwanyun-book --image-scanning-configuration scanOnPush=true --region ap-northeast-1
aws ecr create-repository --repository-name junhwanyun-gateway --image-scanning-configuration scanOnPush=true --region ap-northeast-1
aws ecr create-repository --repository-name junhwanyun-library --image-scanning-configuration scanOnPush=true --region ap-northeast-1

### docker build
mvn clean
mvn package
docker build -t junhwanyun-admin .

mvn clean
mvn package
docker build -t junhwanyun-book .

mvn clean
mvn package
docker build -t junhwanyun-gateway .

mvn clean
mvn package
docker build -t junhwanyun-library .

### docker tag
docker tag junhwanyun-admin:latest 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-admin:latest
docker tag junhwanyun-book:latest 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest
docker tag junhwanyun-gateway:latest 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-gateway:latest
docker tag junhwanyun-library:latest 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-library:latest

### docker push
docker push 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-admin:latest
docker push 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest
docker push 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-gateway:latest
docker push 496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-library:latest

### create deploy
kubectl create deploy admin --image=496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-admin:latest
kubectl create deploy book --image=496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-book:latest
kubectl create deploy gateway --image=496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-gateway:latest
kubectl create deploy library --image=496278789073.dkr.ecr.ap-northeast-1.amazonaws.com/junhwanyun-library:latest

### expose deploy
kubectl expose deploy admin --type=ClusterIP --port=8080
kubectl expose deploy book --type=ClusterIP --port=8080
kubectl expose deploy library --type=ClusterIP --port=8080

kubectl expose deploy gateway --type=LoadBalancer --port=8080

---

## Local

### command
mvn spring-boot:run
./bin/kafka-console-consumer.sh --topic junhwanyun --from-beginning --bootstrap-server localhost:9092