kubectl create namespace reactive
kubectl create -n reactive -f quota.yml

kubectl create -n reactive -f wiki-deployment.yml
kubectl create -n reactive -f wiki-service.yml
